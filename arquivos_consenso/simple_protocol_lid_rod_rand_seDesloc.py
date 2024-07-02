import enum
import json
import logging
from typing import TypedDict, List, Tuple
from random import randint,choice,shuffle
from gradysim.protocol.interface import IProtocol
from gradysim.protocol.messages.communication import SendMessageCommand, BroadcastMessageCommand
from gradysim.protocol.messages.telemetry import Telemetry,Position
from gradysim.protocol.plugin.mission_mobility import MissionMobilityPlugin, MissionMobilityConfiguration
from collections import Counter
from gradysim.protocol.position import *
from requist import mission_array

def most_common_element(lst):
    if not lst:
        return None  # Retorna None se a lista estiver vazia
    
    counter = Counter(lst)
    most_common = counter.most_common(1)[0]  # Retorna o elemento mais comum e sua contagem
    return most_common[0]  # Retorna apenas o elemento

# Parametros de avaliacão
TOTAL_MSG = 0  # TOTAL DE MENSAGENS
MSG_RODADA = [] # MSG/RODADA
IDENT_RODADA = 0  # IDENTIFICADOR DE RODADA
NUMERO_DE_CONSENSOS_RODADA = [0] # CONSENSOS/RODADA



class SimpleSender(enum.Enum):
    SENSOR = 0
    UAV = 1
    GROUND_STATION = 2

# menssagem de orinetação da missao

class ComplexMsg(TypedDict):
    tipe: str
    packet_count: int
    sender_type: int
    sender_id: int
    act_pos: Position
    decision: int
    Lista: List
    # position: Tuple[int, int, int]  # Especifica que a tupla contém dois inteiros


class SimpleSensorProtocol(IProtocol):
    _log: logging.Logger
    
    packet_count: int
    require: int
    personal_assist: int
    pos: Position

    def initialize(self) -> None:
        self._log = logging.getLogger()
        self.packet_count = 0               # numero de pacotes
        self._pronto()                      # mudar o numero de UAV's
        self.personal_assist = 0            # coletor dos dados
        self.pos = None
        # self._log.info(f"Position {self.pos}")
        self._generate_packet()  
        
    def _pronto(self) -> None:
        self.require = randint(1,1)

    def _generate_packet(self) -> None:    # Gera os pacotes
        self.packet_count += 1
        # self._log.info(f"Generated packet, current count {self.packet_count}")

        # envia Beacon para encontrar o sensor
        if self.require > 0: 
            message: ComplexMsg = {
            'tipe': "beacon",
            'packet_count': 0,
            'sender_type': SimpleSender.SENSOR.value,
            'sender_id': self.provider.get_id(),
            'act_pos': self.pos,
            'decision': 0,
            }
            
            command = BroadcastMessageCommand(json.dumps(message))
            self.provider.send_communication_command(command)

            # programa uma proxima tentativa 
            self.provider.schedule_timer("generate_packet", self.provider.current_time() + 1)
        
        else:
            # programa uma proxima verificação de necessidade
            self.provider.schedule_timer("pronto", self.provider.current_time() + 50)

       
    def _reset_generation(self) -> None:  # Não gerar pacotes enquanto realiza a transferencia
        if self.personal_assist != 0:
            self.personal_assist = 0
            self.packet_count = 0
            self.provider.schedule_timer("generate_packet", self.provider.current_time() + 7)
        # pass
    def handle_timer(self, timer: str) -> None:
        if timer == 'generate_packet':
            self._generate_packet()
        elif timer == 'pronto':
            self._pronto()
        # pass

    def handle_packet(self, message: str) -> None:
        complex_message: ComplexMsg = json.loads(message)

        # #  Transfere os arquivos 

        if complex_message['sender_type'] == SimpleSender.UAV.value and complex_message['tipe'] == "transfer":
                
            self.personal_assist = complex_message['sender_id']
            self.provider.cancel_timer('generate_packet')
            self.provider.cancel_timer('pronto')  
            
            # sensor retorna a quantidade de pacotes
            response: ComplexMsg = {
            'tipe': "transfer",
            'packet_count': self.packet_count,
            'sender_type': SimpleSender.SENSOR.value,
            'sender_id': self.provider.get_id(),
            'act_pos': self.pos,
            'decision': 0,
            }
                
            command = SendMessageCommand(json.dumps(response),  complex_message['sender_id'])
            self.provider.send_communication_command(command)

            global TOTAL_MSG
            TOTAL_MSG += self.packet_count
            self._reset_generation()
            # pass
        
    def handle_telemetry(self, telemetry: Telemetry) -> None:
        self.pos =  telemetry.current_position

    def finish(self) -> None:
        self._log.info(f"Final packet count: {self.packet_count}")
        # pass



real_mission = mission_array.copy()

class SimpleUAVProtocol(IProtocol):

    _log: logging.Logger

    packet_count: int
    leader: int
    pos: Position
    init_position: Position
    sensor_resp: int

    objective_pos: Position
    estimated: List 
    leaders: List
    choice_ele: int
    choice_pos: Position
    frst: bool
    sensor_id: int

    _mission: MissionMobilityPlugin

    def initialize(self) -> None:
        self._log = logging.getLogger()
        self.frst = True
        self.leaders = []
        self.choice_ele = self.provider.get_id()
        self.packet_count = 0
        self.leader = 0
        self.objective_pos = (0,0,0)
        self.init_position = (0,0,0)
        self.choice_pos = (0,0,0)
        self.sensor_resp = -1
        self.estimated = []
        self.sensor_id = -1

        self._mission = MissionMobilityPlugin(self, MissionMobilityConfiguration(
           #  loop_mission=LoopMission.RESTART,
        ))

        self._mission.stop_mission()
        
        self._send_heartbeat()


    def _send_heartbeat(self) -> None:

        self._log.info(f"Sending heartbeat, current count {self.packet_count}")

        self.frst = True
        
        self.choice_ele = self.provider.get_id()
        self.packet_count = 0
        self.leader = 0
        self.objective_pos = (0,0,0)
        self.init_position = (0,0,0)
        self.choice_pos = (0,0,0)
        self.sensor_resp = -1
        self.estimated = []
        self.sensor_id = -1

        message: ComplexMsg = {
            'tipe': "hb",
            'packet_count': 0,
            'sender_type': SimpleSender.UAV.value,
            'sender_id': self.provider.get_id(),
            'act_pos': (0,0,0),
            'decision': 1,
            }


        command = BroadcastMessageCommand(json.dumps(message))
        self.provider.send_communication_command(command)
        # self.provider.schedule_timer('heart_beat', self.provider.current_time() + 1)

    def _leader_consensus_state(self) -> None:
        self.frst = False
        # self._log.info(f"{self.leader}  hype")     
        message: ComplexMsg = {
            'tipe': "consensus_elec",
            'packet_count': self.packet_count,
            'sender_type': SimpleSender.UAV.value,
            'sender_id': self.provider.get_id(),
            'act_pos': self.objective_pos,
            'decision': self.sensor_id,
            }


        command = BroadcastMessageCommand(json.dumps(message))
        self.provider.send_communication_command(command)

        self.provider.schedule_timer('consensus_timer', self.provider.current_time() + 12)

    def _consensus_state(self) -> None:
        self.frst = False
        # self._log.info(f"{self.leader}  type")
        message: ComplexMsg = {
            'tipe': "consensus_req",
            'packet_count': self.packet_count,
            'sender_type': SimpleSender.UAV.value,
            'sender_id': self.provider.get_id(),
            'act_pos': self.objective_pos,
            'decision': self.sensor_id,
            }

        command = SendMessageCommand(json.dumps(message),  self.leader)
        self.provider.send_communication_command(command)

    def _send_vote (self) -> None:
        # self._log.info(f"{self.objective_pos}  type")
        if self.provider.get_id() != self.leader:

            message: ComplexMsg = {
            'tipe': "consensus_vote",
            'packet_count': self.packet_count,
            'sender_type': SimpleSender.UAV.value,
            'sender_id': self.provider.get_id(),
            'act_pos': self.objective_pos,
            'decision': self.choice_ele,
            }

            command = SendMessageCommand(json.dumps(message),  self.leader)
            self.provider.send_communication_command(command)

    def _consensus(self) -> None:

        # self.choice_ele = most_common_element(self.estimated)
        if self.choice_ele in self.leaders:
            self.leaders.remove(self.choice_ele)
            if self.leaders != []:
                self.leader = choice(self.leaders)

        message: ComplexMsg = {
            'tipe': "consensus_results",
            'packet_count': self.leader,
            'sender_type': SimpleSender.UAV.value,
            'sender_id': self.provider.get_id(),
            'act_pos': self.objective_pos,
            'decision': self.choice_ele,
            'Lista': self.leaders
            }

        command = BroadcastMessageCommand(json.dumps(message))
        self.provider.send_communication_command(command)
        self.frst = True
        self.choice_ele = self.provider.get_id()
        # self.leader = 0
        self.objective_pos = (0,0,0)
        self.choice_pos = (0,0,0)
        self.estimated = []
        self.sensor_id = None

    def _wait_to_pos(self) -> None:

        # self._log.info(f"{self.sensor_id}          type")

        message: ComplexMsg = {
            'tipe': "transfer",
            'packet_count': 0,
            'sender_type': SimpleSender.UAV.value,
            'sender_id': self.provider.get_id(),
            'act_pos': self.pos,
            'decision': 0,
        } 

        command = SendMessageCommand(json.dumps(message),  self.sensor_id)
        self.provider.send_communication_command(command)


    def handle_timer(self, timer: str) -> None:
        if timer == 'heart_beat':
            self._send_heartbeat()
        elif timer == 'consensus_vote':
            # self.provider.cancel_timer('consensus_vote')
            self._send_vote()
        elif timer == 'consensus_timer':
            if self.leader == self.provider.get_id():
                self._consensus()
        elif timer == 'wait_pos_timer':
                self._wait_to_pos()

    def handle_packet(self, message: str) -> None:
        # configura a liberação para a missao
        mission_message: ComplexMsg = json.loads(message)

        if mission_message['sender_type'] == SimpleSender.GROUND_STATION.value and mission_message['tipe'] == "init":
            if self._mission.is_idle:
                self.leaders = mission_message['Lista']
                self.position = mission_array.pop()
                self.init_position = self.position[5]           # o ultimo waypoint hardcode
                self.leader = self.leaders[0]
                # self._log.info(f"{self.leaders}       leader")
                self._mission.start_mission(self.position)
        
        elif mission_message['sender_type'] == SimpleSender.UAV.value and mission_message['tipe'] == "consensus_req":
            # self._log.info(f"{self.frst}  type")
            self.sensor_id = mission_message['decision']
            self.objective_pos = mission_message['act_pos']
            if self.leader == self.provider.get_id() and self.frst:
                self._leader_consensus_state()
        
        elif mission_message['sender_type'] == SimpleSender.UAV.value and mission_message['tipe'] == "consensus_elec":
            self.objective_pos = mission_message['act_pos']
            
            self.sensor_id = mission_message['decision']
            
            self.frst = False
            message: ComplexMsg = {
                    'tipe': "consensus_resp",
                    'packet_count': self.packet_count,
                    'sender_type': SimpleSender.UAV.value,
                    'sender_id': self.provider.get_id(),
                    'act_pos': self.pos,
                    'decision': 0,
                }

            command = BroadcastMessageCommand(json.dumps(message))
            self.provider.send_communication_command(command)
        

        elif mission_message['sender_type'] == SimpleSender.UAV.value and mission_message['tipe'] == "consensus_resp":
            
            if squared_distance(mission_message['act_pos'],self.objective_pos) > squared_distance(self.pos,self.objective_pos):
               self.choice_ele = mission_message['sender_id'] 
               self.choice_pos = mission_message['act_pos']

            self.provider.schedule_timer('consensus_vote', self.provider.current_time() + 5)

        elif mission_message['sender_type'] == SimpleSender.UAV.value and mission_message['tipe'] == "consensus_vote": 
            self.estimated.append(mission_message['decision'])
            

        elif mission_message['sender_type'] == SimpleSender.UAV.value and mission_message['tipe'] == "consensus_results":
            
            if self.provider.get_id() == mission_message['decision']:
                global NUMERO_DE_CONSENSOS_RODADA,IDENT_RODADA
                NUMERO_DE_CONSENSOS_RODADA[IDENT_RODADA] += 1
                self.sensor_resp = self.sensor_id
                # self._log.info(f"{self.sensor_id}  type")
                # self._log.info(f"{mission_message['sender_id']} lender")
                self._mission.stop_mission()
                # self._log.info(f"{self.init_position} posos" )
                # self._log.info(f"{self.objective_pos} pososition" )
                # self.frst = True
                self._mission.start_mission((self.pos,self.objective_pos))
            else:
                self.frst = True
                self.choice_ele = self.provider.get_id()
                self._log.info(f"{self.leader} pososition" )
                self.leaders = mission_message['Lista']
                self.leader = mission_message['packet_count']
                self._log.info(f"{self.leader} pososition" )
                self.objective_pos = (0,0,0)
                self.choice_pos = (0,0,0)
                self.estimated = []
                self.sensor_id = None

        elif mission_message['sender_type'] == SimpleSender.SENSOR.value and self.frst:
            self._log.info(f" consensus")
            self.objective_pos = mission_message['act_pos']
            self.sensor_id = mission_message['sender_id']
            if self.leader == self.provider.get_id():
                self._leader_consensus_state()
            else:
                self._consensus_state()

        elif mission_message['sender_type'] == SimpleSender.SENSOR.value and mission_message['sender_id'] == self.sensor_resp and mission_message['tipe'] != "transfer":
            # self._log.info(f" muda")
            self.sensor_id = self.sensor_resp
            self.sensor_resp = -1
            self.provider.schedule_timer('wait_pos_timer', self.provider.current_time() + 30)
            
        elif mission_message['sender_type'] == SimpleSender.SENSOR.value and mission_message['sender_id'] == self.sensor_id and mission_message['tipe'] == "transfer":
                self.packet_count += mission_message['packet_count']
                # self.sensor_resp = -1
                # self._log.info(f"{mission_message['sender_id']} ender")
                self._mission.stop_mission()

                # self._log.info(f"{self.init_position}  init pos")
                self._mission.start_mission((self.pos,self.init_position))

    def _transfer_packets(self) -> None:
        self.init_position = (0,0,0)
        message: ComplexMsg = {
            'tipe': "transfer",
            'packet_count': self.packet_count,
            'sender_type': SimpleSender.UAV.value,
            'sender_id': self.provider.get_id(),
            'act_pos': self.pos,
            'decision': 0,
            }

        self.packet_count = 0
        command = BroadcastMessageCommand(json.dumps(message))
        self.provider.send_communication_command(command)

        self.provider.schedule_timer('heart_beat', self.provider.current_time() + 5)

    def handle_telemetry(self, telemetry: Telemetry) -> None:
            self.pos =  telemetry.current_position
            if self.pos == self.init_position:
                self._transfer_packets()
            # self._log.info(f"Position: {self.position}")


    def finish(self) -> None:
        self._log.info(f"Final packet count: {self.packet_count}")
        # pass


class SimpleGroundStationProtocol(IProtocol):
    _log: logging.Logger
    uav_list: List
    packet_count: int
    uav_cord: int
    

    def initialize(self) -> None:
        self._log = logging.getLogger()
        self.uav_list = []
        self.packet_count = 0
        self.uav_cord = 0

    def _config_mission(self) -> None:
        shuffle(self.uav_list)
        self.uav_cord = 6
        self.provider.cancel_timer("start_mission")
        self.provider.schedule_timer("start_mission", self.provider.current_time() + 5)

    def _send_missionConfirmation(self) -> None:

        global MSG_RODADA,IDENT_RODADA,TOTAL_MSG,NUMERO_DE_CONSENSOS_RODADA
        MSG_RODADA.append(TOTAL_MSG)
        NUMERO_DE_CONSENSOS_RODADA.append(0)
        IDENT_RODADA += 1


        self._log.info(f"Sending mission Confirmation ")
        message: ComplexMsg = {
            'tipe': "init",
            'packet_count': 0,
            'sender_type': SimpleSender.GROUND_STATION.value,
            'sender_id': self.provider.get_id(),
            'act_pos': (0,0,0),
            'decision': self.uav_cord,
            'Lista': self.uav_list
            }

        com = BroadcastMessageCommand(json.dumps(message))
        self.provider.send_communication_command(com)
        
        global mission_array
        mission_array = real_mission.copy()
        self.uav_list = []
        # self.provider.cancel_timer("start_mission")

    def handle_timer(self, timer: str) -> None:
        if timer == "start_mission":
            self._send_missionConfirmation()
        if timer == "config_mission":
            self._config_mission()

    def handle_packet(self, message: str) -> None:
        GrounSt_message: ComplexMsg = json.loads(message)
        
        # Iniciar missao

        if  GrounSt_message['sender_type'] == SimpleSender.UAV.value and GrounSt_message['decision'] == 1:
            if GrounSt_message['tipe'] == "hb":
                # self._log.info(f"{self.uav_list}")
                self.uav_list.append(GrounSt_message['sender_id'])      
                if len(self.uav_list) == 5:
                    # self._log.info(f"{self.uav_list}")
                    self.provider.cancel_timer("config_mission")
                    self.provider.schedule_timer("config_mission", self.provider.current_time() + 5)

        elif GrounSt_message['sender_type'] == SimpleSender.UAV.value and GrounSt_message['decision'] == 0:
            if GrounSt_message['tipe']== "transfer":
                self.packet_count += GrounSt_message['packet_count']
                # self.uav_list = []
        # pass

    def handle_telemetry(self, telemetry: Telemetry) -> None:
        pass

    def finish(self) -> None:
        global TOTAL_MSG,MSG_RODADA,IDENT_RODADA,NUMERO_DE_CONSENSOS_RODADA
        self._log.info(f"Final packet count: {self.packet_count} of {TOTAL_MSG} enviadas")
        for i in range(IDENT_RODADA):
            self._log.info(f"Resultados da rodada: {i} com {MSG_RODADA[i]} msgs enviadas com {NUMERO_DE_CONSENSOS_RODADA[i]} Consensos")
        # pass
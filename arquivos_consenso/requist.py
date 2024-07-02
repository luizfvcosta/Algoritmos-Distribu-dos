from random import randint


sensor_position_list = [
( 75+randint(-10,10), 75+randint(-10,10), 0),
(-75+randint(-10,10), 75+randint(-10,10), 0),
( 75+randint(-10,10), -75+randint(-10,10), 0),
(-75+randint(-10,10), -75+randint(-10,10), 0),
]


mission_list = [
        (75, 75 ,20),
        (75, -75, 20),
        (-75, -75, 20),
        (-75,75,20),
        (75, 75 ,20),
        (150,150,0)
]
# Offset for the followers
offset_drone1 = (10, -10)
offset_drone2 = (-10, 10)
offset_drone3 = (10, 10)
offset_drone4 = (-10, -10)


# Generate waypoints for the two follower drones
mission_list_drone1 = [(x + offset_drone1[0], y + offset_drone1[1], z) for x, y, z in mission_list]
mission_list_drone2 = [(x + offset_drone2[0], y + offset_drone2[1], z) for x, y, z in mission_list]
mission_list_drone3 = [(x + offset_drone3[0], y + offset_drone3[1], z) for x, y, z in mission_list]
mission_list_drone4 = [(x + offset_drone4[0], y + offset_drone4[1], z) for x, y, z in mission_list]

# Creating the output array containing all lists

mission_array = [
    mission_list,           # Leader waypoints
    mission_list_drone1,    # Follower drone 1 waypoints
    mission_list_drone2,    # Follower drone 2 waypoints
    mission_list_drone3,    # Follower drone 3 waypoints         
    mission_list_drone4,    # Follower drone 4 waypoints
]
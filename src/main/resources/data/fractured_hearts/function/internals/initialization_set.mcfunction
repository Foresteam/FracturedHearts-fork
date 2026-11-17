# Created by Renthy @ https://modrinth.com/user/Renthy
execute store result score @s fractured_hearts.original_health run attribute @s minecraft:max_health base get 1000
execute store result score @s fractured_hearts.score run attribute @s minecraft:max_health get
scoreboard players operation @s fractured_hearts.hearts = @s fractured_hearts.score
scoreboard players operation @s fractured_hearts.hearts /= #2 fractured_hearts.toggle
scoreboard players enable @s fractured_hearts_toggle_sounds
scoreboard players enable @s fractured_hearts_toggle_messages
scoreboard players enable @s fractured_hearts_toggle_permadeath
scoreboard players enable @s fractured_hearts_status
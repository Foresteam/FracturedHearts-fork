# Created by Renthy @ https://modrinth.com/user/Renthy
scoreboard players operation @s fractured_hearts.score += $fractured_hearts.fakeplayer fractured_hearts.gain_inc
scoreboard players operation #apple_gain fractured_hearts.toggle = $fractured_hearts.fakeplayer fractured_hearts.gain_inc
execute if score @s fractured_hearts.score <= $fractured_hearts.fakeplayer fractured_hearts.ceiling run function fractured_hearts:health_gain/update_attribute
execute if score @s fractured_hearts.score > $fractured_hearts.fakeplayer fractured_hearts.ceiling run function fractured_hearts:health_gain/overflow

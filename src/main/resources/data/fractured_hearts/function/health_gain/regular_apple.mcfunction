advancement revoke @s only fractured_hearts:regular_health_gain
execute unless score #regular_apple_cap fractured_hearts.toggle matches 1.. run return 0
execute unless score @s fractured_hearts.score matches -2147483648..2147483647 run function fractured_hearts:internals/initialization_set
execute unless score @s fractured_hearts.score matches 1.. run return 0

# Limit the gain to the space below both caps.
scoreboard players operation #apple_gain fractured_hearts.toggle = #regular_apple_cap fractured_hearts.toggle
scoreboard players operation #apple_gain fractured_hearts.toggle < $fractured_hearts.fakeplayer fractured_hearts.ceiling
scoreboard players operation #apple_gain fractured_hearts.toggle -= @s fractured_hearts.score
scoreboard players operation #apple_gain fractured_hearts.toggle < $fractured_hearts.fakeplayer fractured_hearts.gain_inc
execute unless score #apple_gain fractured_hearts.toggle matches 1.. run return 0
scoreboard players operation @s fractured_hearts.score += #apple_gain fractured_hearts.toggle
function fractured_hearts:health_gain/update_attribute

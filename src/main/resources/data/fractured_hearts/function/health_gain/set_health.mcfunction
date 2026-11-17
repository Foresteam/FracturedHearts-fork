# Created by Renthy @ https://modrinth.com/user/Renthy
$attribute @s minecraft:max_health base set $(gain)
scoreboard players operation @s fractured_hearts.hearts = @s fractured_hearts.score
scoreboard players operation @s fractured_hearts.hearts /= #2 fractured_hearts.toggle
function fractured_hearts:health_gain/messages
playsound minecraft:block.chorus_flower.grow master @s[tag=!fractured_hearts.sounds_off] ~ ~ ~ 1 0.8
playsound minecraft:block.beacon.power_select master @s[tag=!fractured_hearts.sounds_off] ~ ~ ~ 0.4 1.4
execute unless entity @s[tag=fractured_hearts.particles_off] run particle minecraft:happy_villager ~ ~1 ~ 0.5 0.5 0.5 0 20 force
execute unless entity @s[tag=fractured_hearts.particles_off] run particle minecraft:composter ~ ~1 ~ 0.3 0.5 0.3 0 10 force
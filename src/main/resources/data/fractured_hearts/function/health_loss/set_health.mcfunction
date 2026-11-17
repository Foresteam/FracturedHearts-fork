# Created by Renthy @ https://modrinth.com/user/Renthy
$attribute @s minecraft:max_health base set $(loss)
scoreboard players operation @s fractured_hearts.hearts = @s fractured_hearts.score
scoreboard players operation @s fractured_hearts.hearts /= #2 fractured_hearts.toggle
function fractured_hearts:health_loss/messages
playsound minecraft:block.glass.break master @s[tag=!fractured_hearts.sounds_off] ~ ~ ~ 1 1.3
playsound minecraft:particle.soul_escape master @s[tag=!fractured_hearts.sounds_off] ~ ~ ~ 1 0.6
execute unless entity @s[tag=fractured_hearts.particles_off] run particle minecraft:damage_indicator ~ ~1 ~ 0.5 0.5 0.5 0 15 force
execute unless entity @s[tag=fractured_hearts.particles_off] run particle minecraft:dust{color:[1.0,0.0,0.0],scale:1.5} ~ ~1 ~ 0.4 0.5 0.4 0 10 force
# Created by Renthy @ https://modrinth.com/user/Renthy
scoreboard players operation @s fractured_hearts.score -= $fractured_hearts.fakeplayer fractured_hearts.gain_inc
tellraw @s [{"text":"Unable to gain max health! You are already at maximum health.","color":"gray"}]
playsound minecraft:block.chorus_flower.death master @s[tag=!fractured_hearts.sounds_off] ~ ~ ~ 1 0.8
playsound minecraft:block.fungus.break master @s[tag=!fractured_hearts.sounds_off] ~ ~ ~ 1 0.6
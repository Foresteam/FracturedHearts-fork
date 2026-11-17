# Created by Renthy @ https://modrinth.com/user/Renthy
scoreboard players set #2 fractured_hearts.toggle 2
scoreboard players set #3 fractured_hearts.toggle 3
scoreboard players set #4 fractured_hearts.toggle 4
scoreboard players operation @s fractured_hearts.toggle = @s fractured_hearts.hearts
execute store result score #max fractured_hearts.toggle run scoreboard players get $fractured_hearts.fakeplayer fractured_hearts.ceiling
scoreboard players operation #max fractured_hearts.toggle /= #2 fractured_hearts.toggle
execute store result score #threshold_high fractured_hearts.toggle run scoreboard players get #max fractured_hearts.toggle
scoreboard players operation #threshold_high fractured_hearts.toggle *= #3 fractured_hearts.toggle
scoreboard players operation #threshold_high fractured_hearts.toggle /= #4 fractured_hearts.toggle
execute store result score #threshold_mid fractured_hearts.toggle run scoreboard players get #max fractured_hearts.toggle
scoreboard players operation #threshold_mid fractured_hearts.toggle /= #2 fractured_hearts.toggle
execute if score @s fractured_hearts.hearts >= #threshold_high fractured_hearts.toggle run tellraw @s [{"text":"Current Hearts: ","color":"gray"},{"score":{"name":"@s","objective":"fractured_hearts.hearts"},"color":"green"},{"text":" ❤","color":"green"}]
execute if score @s fractured_hearts.hearts < #threshold_high fractured_hearts.toggle if score @s fractured_hearts.hearts >= #threshold_mid fractured_hearts.toggle run tellraw @s [{"text":"Current Hearts: ","color":"gray"},{"score":{"name":"@s","objective":"fractured_hearts.hearts"},"color":"yellow"},{"text":" ❤","color":"yellow"}]
execute if score @s fractured_hearts.hearts < #threshold_mid fractured_hearts.toggle run tellraw @s [{"text":"Current Hearts: ","color":"gray"},{"score":{"name":"@s","objective":"fractured_hearts.hearts"},"color":"red"},{"text":" ❤","color":"red"}]
tellraw @s [{"text":"","color":"gray"}]
tellraw @s [{"text":"Server Settings:","color":"yellow","bold":true}]
execute if score $fractured_hearts.fakeplayer fractured_hearts.permadeath matches 1 run tellraw @s [{"text":"Permadeath: ","color":"gray"},{"text":"ENABLED","color":"red"}]
execute unless score $fractured_hearts.fakeplayer fractured_hearts.permadeath matches 1 run tellraw @s [{"text":"Permadeath: ","color":"gray"},{"text":"Disabled","color":"green"}]
execute if score $fractured_hearts.fakeplayer fractured_hearts.heart_steal matches 1 run tellraw @s [{"text":"PvP Heart Steal: ","color":"gray"},{"text":"ENABLED","color":"red"}]
execute unless score $fractured_hearts.fakeplayer fractured_hearts.heart_steal matches 1 run tellraw @s [{"text":"PvP Heart Steal: ","color":"gray"},{"text":"Disabled","color":"green"}]
tellraw @s [{"text":"","color":"gray"}]
tellraw @s [{"text":"Personal Settings:","color":"yellow","bold":true}]
execute if entity @s[tag=fractured_hearts.sounds_off] run tellraw @s [{"text":"Sounds: ","color":"gray"},{"text":"OFF","color":"red"}]
execute unless entity @s[tag=fractured_hearts.sounds_off] run tellraw @s [{"text":"Sounds: ","color":"gray"},{"text":"ON","color":"green"}]
execute if entity @s[tag=fractured_hearts.messages_off] run tellraw @s [{"text":"Messages: ","color":"gray"},{"text":"OFF","color":"red"}]
execute unless entity @s[tag=fractured_hearts.messages_off] run tellraw @s [{"text":"Messages: ","color":"gray"},{"text":"ON","color":"green"}]
execute if entity @s[tag=fractured_hearts.particles_off] run tellraw @s [{"text":"Particles: ","color":"gray"},{"text":"OFF","color":"red"}]
execute unless entity @s[tag=fractured_hearts.particles_off] run tellraw @s [{"text":"Particles: ","color":"gray"},{"text":"ON","color":"green"}]
scoreboard players set @s fractured_hearts_status 0
scoreboard players enable @s fractured_hearts_status
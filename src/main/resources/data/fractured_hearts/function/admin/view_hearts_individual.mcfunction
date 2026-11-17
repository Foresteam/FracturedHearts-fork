# Created by Renthy @ https://modrinth.com/user/Renthy
scoreboard players set #2 fractured_hearts.toggle 2
scoreboard players set #3 fractured_hearts.toggle 3
scoreboard players set #4 fractured_hearts.toggle 4
execute store result score #max fractured_hearts.toggle run scoreboard players get $fractured_hearts.fakeplayer fractured_hearts.ceiling
scoreboard players operation #max fractured_hearts.toggle /= #2 fractured_hearts.toggle
execute store result score #threshold_high fractured_hearts.toggle run scoreboard players get #max fractured_hearts.toggle
scoreboard players operation #threshold_high fractured_hearts.toggle *= #3 fractured_hearts.toggle
scoreboard players operation #threshold_high fractured_hearts.toggle /= #4 fractured_hearts.toggle
execute store result score #threshold_mid fractured_hearts.toggle run scoreboard players get #max fractured_hearts.toggle
scoreboard players operation #threshold_mid fractured_hearts.toggle /= #2 fractured_hearts.toggle
execute if score @s fractured_hearts.hearts >= #threshold_high fractured_hearts.toggle run tellraw @a[tag=fractured_hearts.viewing_hearts] [{"text":"• ","color":"gray"},{"selector":"@s","color":"green"},{"text":": ","color":"gray"},{"score":{"name":"@s","objective":"fractured_hearts.hearts"},"color":"green"},{"text":" ❤","color":"green"}]
execute if score @s fractured_hearts.hearts < #threshold_high fractured_hearts.toggle if score @s fractured_hearts.hearts >= #threshold_mid fractured_hearts.toggle run tellraw @a[tag=fractured_hearts.viewing_hearts] [{"text":"• ","color":"gray"},{"selector":"@s","color":"yellow"},{"text":": ","color":"gray"},{"score":{"name":"@s","objective":"fractured_hearts.hearts"},"color":"yellow"},{"text":" ❤","color":"yellow"}]
execute if score @s fractured_hearts.hearts < #threshold_mid fractured_hearts.toggle run tellraw @a[tag=fractured_hearts.viewing_hearts] [{"text":"• ","color":"gray"},{"selector":"@s","color":"red"},{"text":": ","color":"gray"},{"score":{"name":"@s","objective":"fractured_hearts.hearts"},"color":"red"},{"text":" ❤","color":"red"}]
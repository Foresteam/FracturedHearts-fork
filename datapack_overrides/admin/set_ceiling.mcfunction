# Created by Renthy @ https://modrinth.com/user/Renthy
$scoreboard players set #new_ceiling fractured_hearts.toggle $(value)
execute if score #new_ceiling fractured_hearts.toggle matches ..0 run tellraw @a [{"text":"Error: Ceiling must be positive","color":"red"}]
execute if score #new_ceiling fractured_hearts.toggle matches ..0 run return 0
scoreboard players operation $fractured_hearts.fakeplayer fractured_hearts.ceiling = #new_ceiling fractured_hearts.toggle
execute as @a if score @s fractured_hearts.score > $fractured_hearts.fakeplayer fractured_hearts.ceiling run tag @s add fractured_hearts.needs_clamp
execute as @a[tag=fractured_hearts.needs_clamp] run scoreboard players operation @s fractured_hearts.score = $fractured_hearts.fakeplayer fractured_hearts.ceiling
execute as @a[tag=fractured_hearts.needs_clamp] run function fractured_hearts:health_gain/update_attribute
execute as @a[tag=fractured_hearts.needs_clamp] run tag @s remove fractured_hearts.needs_clamp
execute store result score #hearts fractured_hearts.toggle run scoreboard players get $fractured_hearts.fakeplayer fractured_hearts.ceiling
scoreboard players set #2 fractured_hearts.toggle 2
scoreboard players operation #hearts fractured_hearts.toggle /= #2 fractured_hearts.toggle
tellraw @a [{"text":"Health ceiling set to ","color":"gray"},{"score":{"name":"#hearts","objective":"fractured_hearts.toggle"},"color":"green"},{"text":" hearts","color":"gray"}]
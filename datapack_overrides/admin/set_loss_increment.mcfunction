# Created by Renthy @ https://modrinth.com/user/Renthy
$scoreboard players set #new_loss fractured_hearts.toggle $(value)
execute if score #new_loss fractured_hearts.toggle matches ..0 run tellraw @a [{"text":"Error: Loss increment must be positive","color":"red"}]
execute if score #new_loss fractured_hearts.toggle matches ..0 run return 0
scoreboard players operation $fractured_hearts.fakeplayer fractured_hearts.loss_inc = #new_loss fractured_hearts.toggle
execute store result score #hearts fractured_hearts.toggle run scoreboard players get $fractured_hearts.fakeplayer fractured_hearts.loss_inc
scoreboard players set #2 fractured_hearts.toggle 2
scoreboard players operation #hearts fractured_hearts.toggle /= #2 fractured_hearts.toggle
tellraw @a [{"text":"Health loss increment set to ","color":"gray"},{"score":{"name":"#hearts","objective":"fractured_hearts.toggle"},"color":"red"},{"text":" heart per death","color":"gray"}]
# Created by Renthy @ https://modrinth.com/user/Renthy
execute store result storage fractured_hearts:data temp int 1 run scoreboard players get @s fractured_hearts.score
function fractured_hearts:admin/set_player_hearts_attribute with storage fractured_hearts:data
scoreboard players operation @s fractured_hearts.hearts = @s fractured_hearts.score
scoreboard players operation @s fractured_hearts.hearts /= #2 fractured_hearts.toggle
tellraw @a [{"selector":"@s","color":"yellow"},{"text":"'s hearts have been set to ","color":"gray"},{"score":{"name":"@s","objective":"fractured_hearts.hearts"},"color":"aqua"},{"text":" ❤","color":"aqua"}]
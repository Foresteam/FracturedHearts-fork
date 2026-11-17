execute store result storage fractured_hearts:data temp int 1 run scoreboard players get @s fractured_hearts.score
function fractured_hearts:admin/set_player_hearts_attribute with storage fractured_hearts:data
scoreboard players operation @s fractured_hearts.hearts = @s fractured_hearts.score
scoreboard players operation @s fractured_hearts.hearts /= #2 fractured_hearts.toggle

# Created by Renthy @ https://modrinth.com/user/Renthy
scoreboard players operation @s fractured_hearts.score += $fractured_hearts.fakeplayer fractured_hearts.loss_inc
execute store result storage fractured_hearts:data loss int 1 run scoreboard players get @s fractured_hearts.score
function fractured_hearts:health_loss/perish_set with storage fractured_hearts:data
execute if score $fractured_hearts.fakeplayer fractured_hearts.permadeath matches 1 run fracturedhearts markpermadeath
execute if score $fractured_hearts.fakeplayer fractured_hearts.permadeath matches 1 run gamemode spectator @s
execute if score $fractured_hearts.fakeplayer fractured_hearts.permadeath matches 1 run title @s title {"text":"GAME OVER","color":"dark_red","bold":true}
execute if score $fractured_hearts.fakeplayer fractured_hearts.permadeath matches 1 run title @s subtitle {"text":"You have perished","color":"red"}
execute unless score $fractured_hearts.fakeplayer fractured_hearts.permadeath matches 1 unless entity @s[tag=fractured_hearts.messages_off] if score @s fractured_hearts.hearts matches 1 run tellraw @s [{"text":"You're down to your last heart!","color":"dark_red"},{"text":" Don't die again...","color":"red"}]
execute unless score $fractured_hearts.fakeplayer fractured_hearts.permadeath matches 1 unless entity @s[tag=fractured_hearts.messages_off] if score @s fractured_hearts.hearts matches 2..3 run tellraw @s [{"text":"You're running dangerously low on hearts!","color":"red"}]
execute unless score $fractured_hearts.fakeplayer fractured_hearts.permadeath matches 1 unless entity @s[tag=fractured_hearts.messages_off] if score @s fractured_hearts.hearts matches 4..5 run tellraw @s [{"text":"Your health is critically low. Be careful!","color":"gold"}]
execute unless score $fractured_hearts.fakeplayer fractured_hearts.permadeath matches 1 unless entity @s[tag=fractured_hearts.messages_off] if score @s fractured_hearts.hearts matches 6..10 run tellraw @s [{"text":"You've reached minimum health. Stay safe!","color":"yellow"}]
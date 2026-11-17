# Created by Renthy @ https://modrinth.com/user/Renthy
$scoreboard players set #target_hearts fractured_hearts.toggle $(hearts)
scoreboard players set #2 fractured_hearts.toggle 2
scoreboard players operation #target_hearts fractured_hearts.toggle *= #2 fractured_hearts.toggle
execute store result score #floor fractured_hearts.toggle run scoreboard players get $fractured_hearts.fakeplayer fractured_hearts.floor
execute store result score #ceiling fractured_hearts.toggle run scoreboard players get $fractured_hearts.fakeplayer fractured_hearts.ceiling
execute if score #target_hearts fractured_hearts.toggle < #floor fractured_hearts.toggle run tellraw @s [{"text":"Error: Cannot set hearts below floor (","color":"red"},{"score":{"name":"#floor","objective":"fractured_hearts.toggle"},"color":"red"},{"text":" health points)","color":"red"}]
execute if score #target_hearts fractured_hearts.toggle < #floor fractured_hearts.toggle run return 0
execute if score #target_hearts fractured_hearts.toggle > #ceiling fractured_hearts.toggle run tellraw @s [{"text":"Error: Cannot set hearts above ceiling (","color":"red"},{"score":{"name":"#ceiling","objective":"fractured_hearts.toggle"},"color":"red"},{"text":" health points)","color":"red"}]
execute if score #target_hearts fractured_hearts.toggle > #ceiling fractured_hearts.toggle run return 0
$execute as @a[name=$(player)] run scoreboard players operation @s fractured_hearts.score = #target_hearts fractured_hearts.toggle
$execute as @a[name=$(player)] run function fractured_hearts:admin/set_player_hearts_apply
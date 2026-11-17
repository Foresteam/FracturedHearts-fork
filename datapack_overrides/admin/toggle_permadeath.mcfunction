# Created by Renthy @ https://modrinth.com/user/Renthy
execute store result score #temp fractured_hearts.toggle run scoreboard players get $fractured_hearts.fakeplayer fractured_hearts.permadeath
execute if score #temp fractured_hearts.toggle matches 1 run scoreboard players set $fractured_hearts.fakeplayer fractured_hearts.permadeath 0
execute unless score #temp fractured_hearts.toggle matches 1 run scoreboard players set $fractured_hearts.fakeplayer fractured_hearts.permadeath 1
execute if score $fractured_hearts.fakeplayer fractured_hearts.permadeath matches 1 run tellraw @a [{"text":"Permadeath has been ","color":"gray"},{"text":"ENABLED","color":"red"},{"text":" server-wide","color":"gray"}]
execute unless score $fractured_hearts.fakeplayer fractured_hearts.permadeath matches 1 run tellraw @a [{"text":"Permadeath has been ","color":"gray"},{"text":"DISABLED","color":"green"},{"text":" server-wide","color":"gray"}]

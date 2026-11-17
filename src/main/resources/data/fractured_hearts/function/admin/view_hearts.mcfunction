# Created by Renthy @ https://modrinth.com/user/Renthy
tag @s add fractured_hearts.viewing_hearts
tellraw @s [{"text":"━━━━━━━━━━━━━━━━━━━━━━━━━━━━","color":"gray"}]
tellraw @s [{"text":"Player Hearts","color":"dark_red","bold":true}]
tellraw @s [{"text":"━━━━━━━━━━━━━━━━━━━━━━━━━━━━","color":"gray"}]
execute as @a run function fractured_hearts:admin/view_hearts_individual
tellraw @s [{"text":"━━━━━━━━━━━━━━━━━━━━━━━━━━━━","color":"gray"}]
tag @s remove fractured_hearts.viewing_hearts
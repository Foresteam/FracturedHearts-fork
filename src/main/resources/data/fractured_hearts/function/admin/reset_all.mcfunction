# Created by Renthy @ https://modrinth.com/user/Renthy
execute as @a run scoreboard players set @s fractured_hearts.score 20
execute as @a run attribute @s minecraft:max_health base set 20
execute as @a run scoreboard players set @s fractured_hearts.hearts 10
tellraw @a [{"text":"All players have been reset to default health (10 hearts)","color":"gray"}]
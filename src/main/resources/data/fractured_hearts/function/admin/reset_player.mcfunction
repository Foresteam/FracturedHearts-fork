# Created by Renthy @ https://modrinth.com/user/Renthy
$execute as @a[name=$(player)] run scoreboard players set @s fractured_hearts.score 20
$execute as @a[name=$(player)] run attribute @s minecraft:max_health base set 20
$execute as @a[name=$(player)] run scoreboard players set @s fractured_hearts.hearts 10
$tellraw @a [{"selector":"@a[name=$(player)]","color":"yellow"},{"text":" has been reset to default health (10 hearts)","color":"gray"}]
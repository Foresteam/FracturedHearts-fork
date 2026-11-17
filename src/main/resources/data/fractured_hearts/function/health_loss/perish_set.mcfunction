# Created by Renthy @ https://modrinth.com/user/Renthy
$attribute @s minecraft:max_health base set $(perish)
scoreboard players operation @s fractured_hearts.hearts = @s fractured_hearts.score
scoreboard players operation @s fractured_hearts.hearts /= #2 fractured_hearts.toggle
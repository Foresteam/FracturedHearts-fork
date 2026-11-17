# Created by Renthy @ https://modrinth.com/user/Renthy
$execute as @s[tag=$(tag)] run scoreboard players set @s fractured_hearts.toggle 1
$execute if score @s fractured_hearts.toggle matches 1 run tag @s remove $(tag)
$execute if score @s fractured_hearts.toggle matches 1 run title @s actionbar [{text:"$(confirm_on)"}]
$execute unless score @s fractured_hearts.toggle matches 1 run tag @s add $(tag)
$execute unless score @s fractured_hearts.toggle matches 1 run title @s actionbar [{text:"$(confirm_off)"}]
execute if score @s fractured_hearts.toggle matches 1 run scoreboard players reset @s fractured_hearts.toggle
$scoreboard players enable @s $(setting)
$scoreboard players set @s $(setting) 0
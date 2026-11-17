# Created by Renthy @ https://modrinth.com/user/Renthy
scoreboard players operation @s fractured_hearts.score -= $fractured_hearts.fakeplayer fractured_hearts.loss_inc
execute if score $fractured_hearts.fakeplayer fractured_hearts.heart_steal matches 1 as @a[tag=fractured_hearts.pvp_killer] run function fractured_hearts:pvp/steal_heart
execute if score @s fractured_hearts.score >= $fractured_hearts.fakeplayer fractured_hearts.floor run function fractured_hearts:health_loss/update_attribute
execute if score @s fractured_hearts.score < $fractured_hearts.fakeplayer fractured_hearts.floor run function fractured_hearts:health_loss/perish
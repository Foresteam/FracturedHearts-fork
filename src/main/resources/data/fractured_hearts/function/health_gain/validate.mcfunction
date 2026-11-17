# Created by Renthy @ https://modrinth.com/user/Renthy
advancement revoke @s only fractured_hearts:health_gain
execute unless score @s fractured_hearts.score matches -2147483648..2147483647 run function fractured_hearts:internals/initialization_set
execute if score @s fractured_hearts.score matches 0 run tellraw @s [{"text":"You are excluded from Fractured Hearts!","color":"gray"}]
execute if score @s fractured_hearts.score matches 0 run return 0
execute unless score $fractured_hearts.fakeplayer fractured_hearts.gain_inc matches 0 run function fractured_hearts:health_gain/apply_gain
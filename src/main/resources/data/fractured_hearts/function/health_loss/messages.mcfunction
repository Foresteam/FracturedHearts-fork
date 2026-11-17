# Created by Renthy @ https://modrinth.com/user/Renthy
data modify storage fractured_hearts:data loss set value [[{text:'a '}],[{text:'amount '}]]
execute if score @s fractured_hearts.score <= $fractured_hearts.fakeplayer fractured_hearts.loss_inc run data modify storage fractured_hearts:data loss[0] set value [{text:'their penultimate '}]
execute if score $fractured_hearts.fakeplayer fractured_hearts.loss_inc matches 3.. run data modify storage fractured_hearts:data loss[0] set value [{text:'an '}]
execute if score $fractured_hearts.fakeplayer fractured_hearts.loss_inc matches 1 run data modify storage fractured_hearts:data loss[1] set value [{text:'half-heart '}]
execute if score $fractured_hearts.fakeplayer fractured_hearts.loss_inc matches 2 run data modify storage fractured_hearts:data loss[1] set value [{text:'heart '}]
execute unless entity @s[tag=fractured_hearts.messages_off] run tellraw @a [{"selector":"@s","color":"red"},{"text":" has surrendered ","color":"gray"},{"storage":"fractured_hearts:data",nbt:"loss[0]",interpret:true,"color":"red"},{"storage":"fractured_hearts:data",nbt:"loss[1]",interpret:true,"color":"red"},{"text":"of max health!","color":"gray"}]
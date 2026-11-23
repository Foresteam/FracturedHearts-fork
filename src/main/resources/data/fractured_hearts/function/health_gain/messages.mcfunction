# Created by Renthy @ https://modrinth.com/user/Renthy
data modify storage fractured_hearts:data gain set value [[{text:'a '}],[{text:'amount '}]]
execute if score @s fractured_hearts.score = $fractured_hearts.fakeplayer fractured_hearts.ceiling run data modify storage fractured_hearts:data gain[0] set value [{text:'their final '}]
execute if score #apple_gain fractured_hearts.toggle matches 3.. run data modify storage fractured_hearts:data gain[0] set value [{text:'an '}]
execute if score #apple_gain fractured_hearts.toggle matches 1 run data modify storage fractured_hearts:data gain[1] set value [{text:'half-heart '}]
execute if score #apple_gain fractured_hearts.toggle matches 2 run data modify storage fractured_hearts:data gain[1] set value [{text:'heart '}]
execute unless entity @s[tag=fractured_hearts.messages_off] run tellraw @a [{"selector":"@s","color":"green"},{"text":" has gained ","color":"gray"},{"storage":"fractured_hearts:data",nbt:"gain[0]",interpret:true,"color":"green"},{"storage":"fractured_hearts:data",nbt:"gain[1]",interpret:true,"color":"green"},{"text":"of max health!","color":"gray"}]

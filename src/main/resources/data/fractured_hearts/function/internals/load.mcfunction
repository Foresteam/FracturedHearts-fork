# Created by Renthy @ https://modrinth.com/user/Renthy
scoreboard objectives add fractured_hearts.score dummy "Health score"
scoreboard objectives add fractured_hearts.hearts dummy "❤"
scoreboard objectives add fractured_hearts.original_health dummy "Original max health × 1000"
scoreboard objectives add fractured_hearts.original_gamemode dummy "Original game mode"
scoreboard objectives setdisplay list fractured_hearts.hearts
scoreboard objectives add fractured_hearts.death minecraft.custom:minecraft.time_since_death "Time since last death"
scoreboard objectives add fractured_hearts.toggle dummy "Toggle"
scoreboard players set #2 fractured_hearts.toggle 2
scoreboard objectives add fractured_hearts.ceiling dummy "Health ceiling"
scoreboard objectives add fractured_hearts.gain_inc dummy "Health gain increment"
scoreboard objectives add fractured_hearts.floor dummy "Health floor"
scoreboard objectives add fractured_hearts.loss_inc dummy "Health loss increment"
scoreboard objectives add fractured_hearts_toggle_sounds trigger "Sounds"
scoreboard objectives add fractured_hearts_toggle_messages trigger "Messages"
scoreboard objectives add fractured_hearts_toggle_particles trigger "Particles"
scoreboard objectives add fractured_hearts_status trigger "Status"
scoreboard objectives add fractured_hearts.permadeath dummy "Permadeath Enabled"
scoreboard objectives add fractured_hearts.heart_steal dummy "Heart Steal Enabled"
execute unless score $fractured_hearts.fakeplayer fractured_hearts.ceiling matches 0.. run function fractured_hearts:options/default_settings
schedule function fractured_hearts:internals/loop 20t replace
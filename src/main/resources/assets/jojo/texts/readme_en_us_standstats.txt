This data pack configures Stand stats in Ripples of the Past mod.
To change the stats of a Stand using this data pack, you'll need to:
1) Find a .json file for a Stand you want to configure.
2) Open it with any text editor and change the values you need.
----
The file has two main blocks of variables:
    statsBase - stats the Stand will have at the start of its progression (Resolve levels, up to 4)
    statsDevPotential - stats by the end of the progression
        The stats progress towards the statsDevPotential values with each Resolve level.

Here is what each stat in these blocks affects:
    power - attacks damage and, to a lesser extent, damage resistance
    speed - attacks speed, movement speed and barrage hit count
    durability - stamina usage and damage resistance
    precision - hitbox detection, projectile accuracy and, to a lesser extent, barrage damage

Each Stand also has two range parameters, relevant for the manual control mode:
    rangeEffective - maximum range (in blocks) at which the Stand doesn't lose its strength
    rangeMax - the Stand cannot move beyond that range
    
The setting called "randomWeight" determines how likely the Stand is to get from an Arrow. By default most Stands have it on 2, but such Stands as Star Platinum or The World have it at 1, so they are twice as rare.
----
3) Save the edited file(s).
4) Open the Minecraft world, or type /reload command in the chat if you're editing it while playing in that world (basically the standard process of data pack loading).

If your Stand was summoned during this process, you'll have to resummon it for the changes to take effect.

This data pack's format will stay the same throughout v0.2.X versions of the mod, however, it may or may not change by the time v0.3 comes out.
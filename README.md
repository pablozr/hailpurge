# HailPurge

The custom campaign mod for Colonia Espacial on Forge 1.20.1.

## Modules

- `biosphere`: Initial spherical Biosphere, Hostile Atmosphere, and atmospheric protection.
- `colony`: Reserved for the Shared Colony, Colonist Reserve, and Colony Morale.
- `expedition`: Reserved for Expedition Drones and Outposts.
- `factions`: Reserved for NPC Factions and the Containment Force.

Each module keeps its rules and persistence close together. External integrations belong in adapters owned by the consuming module, never in the campaign core.

## Atmospheric Protection

The first vertical slice uses item tags so that Hostile Atmosphere rules do not depend on an external mod API.

- `hailpurge:atmosphere/basic`: Any tagged armor piece reduces exposure only during daytime.
- `hailpurge:atmosphere/full`: A tagged complete suit item neutralizes exposure during day and night.

Ad Astra oxygen gear is mapped to basic protection, while its complete space suits are mapped to full protection. Active oxygen consumption is not evaluated in this slice.

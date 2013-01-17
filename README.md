AutoRanker
==========

AutoRanker is a Bukkit plugin that automatically executes commands on players
when certain requirements are met in their LogBlock history. The requirements
are principally specified in the configuration in terms of number of blocks
created or blocks destroyed, and time online.

The intended use case for this plugin is to allow a server operator to have
players automatically promoted through different ranks as the amount of their
work on the server accumulates. Note that this use requires a separate plugin
to facilitate ranking commands, such as bPermissions.

Permissions
-----------

* `autorank.reload` (default: op)
    allows a player to reload the AutoRanker configuration
    using `/autorank reload`.

Configuration
-------------

Provide a list of ranks in the configuration file.
For each rank, provide the set of requirements for achieving that rank,
such as the following:

* blocks placed
* blocks destroyed
* hours spent online
* days since first login
* a prerequisite permission, such as for a base group
* a postrequisite permission, to indicate that a player already has this rank

Example:

```
ranks:
  builder:
    requirements:
      daysonline: 2
      hoursonline: 2
      created: 1000
      destroyed: 2000
      haspermission: group.default
      hasnotpermission: group.builder
    commands:
      - exec u:{PLAYER} a:setgroup v:builder
      - say {PLAYER} has achieved Builder rank.
  architect:
    requirements:
      daysonline: 60
      hoursonline: 24
      created: 10000
      destroyed: 20000
      haspermission: group.builder
      hasnotpermission: group.architect
    commands:
      - exec u:{PLAYER} a:setgroup v:architect
      - say {PLAYER} has achieved Architect rank.
```

Compiling
---------

    ant deps
    ant


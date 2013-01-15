AutoRanker
==========

AutoRanker is a Bukkit plugin that automatically executes commands on players
when certain requirements are met in their LogBlock history. The requirements
are principally specified in the configuration in terms of number of blocks
created or blocks destroyed.

The intended use case for this plugin is to allow a server operator to have
players automatically promoted through different ranks as the amount of their
work on the server accumulates. Note that this use requires a separate plugin
to facilitate ranking commands, such as bPermissions.

Permissions
-----------

* `autorank.reload`
	allows a player to reload the AutoRanker configuration.

Configuration
-------------

Provide a list of ranks in the configuration file.
For each rank, provide the set of requirements for achieving that rank,
such as the following:

* blocks placed
* blocks destroyed
* a prerequisite permission, such as for a base group
* a postrequisite permission, to indicate that a player already has this rank

TODO:
* days since first login
* accumulated time online

Example:

    ranks:
      proficient:
        requirements:
          created: 1000
          destroyed: 2000
          haspermission: group.satisfactory
          hasnotpermission: group.proficient
        commands:
          - promote PLAYER proficient
          - say PLAYER has achieved Proficient rank.
      distinguished:
        requirements:
          created: 10000
          destroyed: 20000
          haspermission: group.proficient
          hasnotpermission: group.distinguished
        commands:
          - promote PLAYER distinguished
          - say PLAYER has achieved Distinguished rank.


Compiling
---------

    ant deps
    ant


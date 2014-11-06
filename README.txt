Leagion

Leagion is a football/field hockey scorinator created by Rejistania of NationStates for use by the NS Sports community. Rejistania has worked on the project since its birth in 2004 just after World Cup 13.
They have given me permission to upload the source, which was open without a license (under the vague, unspoken agreement that the source is in the tarball for your convenience and that if it eliminates you 0:31, erases your harddisk, or does unspeakable things with your pet dog, it is your fault (do send pictures of the unspeakable things though, for... troubleshooting)) and is now licensed under BSD. I have also offered to help develop new features, as I know Java, having done a small course in it in 2013.

Instructions
1. Install the JDK.
2. Unzip leagionwindows.zip or clone git repository.
3. Open a terminal (Mac/Linux) or cmd.exe (Windows).
4. Navigate (cd) to the Leagion folder.
5. Run the command "java leagion".
6. Follow the Prompts

File Input
There is a file input system to run leagues automatically. For this, you give the file as parameter of leagion, eg: java leagion h1sr.lgn
The extension lgn is common but not enforced.

Files have 3 parts:
1. the parameter section
2. the optional fixture section
3. the team section

The parameter section can contain the following parameters:
=regions
  requires an integer parameter.
  Amount of divisasis. A divisasi is a regional subdivision of a league. As an example: the Regionalliga in Germany has 2 divisasi: Nord and Süd.
This parameter can be also used for groups in competitions like in the group stage of the world cup.
=teams
  Requires an integer parameter. 
  The amount of teams per divisasi
=KPB
  No parameter.
  Use the KPB points and classification instead of the previous linear ranks
=modifiers
  No parameter.
  Allow the use of style modifiers. Style modifiers in leagion are in the slightly quixotic leagion way: positive modifiers are defensive, negative modifiers are offensive. The modifiers generally rank between 100 and -100 but boundaries are not enforced by the program because roleplaying reasons might want you to disregard these limits.

=autofixtures
  No parameters.
  Fixtures are created by leagion. The fixture section of the file must not exist for this to be valid. Without further parameters, this sims round-robin matches between the teams in a divisasi.

=2legs
  No parameters.
  This is an optional addition to the =autofixtures parameter and follows it in a new line. Instead of a single round-robin, a double round-robin is simmed.

=eliminate
  No parameters.
  This sims an elimination-style tournament like the DFB-Pokal and I assume all domestic cups. Requires =autofixtures to give more than an error message. Can be used with or without =2legs

=rejistanianmode
  This gives 4 points for a victory, 2 points for a draw and 1 point for a loss to ICly prevent chanceless teams from forfeiting.

=FHWC
  No parameters.
  Sims field hockey instead of soccer matches.

TODO: add lacking parameters

The Parameter section is ended by a /

The fixture section contains the manually defined fixtures in the form home divisasi home team away divisasi away team. So it the 2nd team of the 1st divisasi plays the 4th team of the 3rd divisasi, the line would be:
1 2 3 4

TODO: explain predefined macros.

This section ends by a line containing just -5.

The team section has the rank of the team either in linear or KPB format (depending on whether you did set =KPB), optionally the style modifier (if you set =modifiers) and the name of the team. These lines are by divisasi, so first, you define the 1st team of the 1st divisasi, then the 2nd team of the 1st divisasi,... then the last team of the 1st divisasi, then the 1st team of the 2nd divisasi... there is no visible separation between different divisasis.
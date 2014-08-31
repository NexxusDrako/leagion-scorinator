class EFileError extends Exception
{}

class TFixture
{
  TFixture next;
  int home_region, away_region, home, away;
  boolean assigned;
}

class TMatch
{
  TMatch next, first_leg;
  int home_region, away_region, home_nr, away_nr;
  int h_regtime, a_regtime, h_et, a_et, h_pk, a_pk, home, away;
  String home_team, away_team;
  int scorination_level; /*0: not simmed, 1: reg time, 2: AET, 3: APK*/
  
  public int winner()
  {
//    Out.println(home_team + "  " + away_team);
    if (first_leg==null)
    {
      if (home>away)
      {
        if (home_nr>away_nr)
        {
          return 1;
        } else
        {
          return 0;
        }
      } else
      {
        if (home_nr>away_nr)
        {
          return 0;
        } else
        {
          return 1;
        }
      }
    }else
    {
      if ((((home+first_leg.away)!=(away+first_leg.home))&&(home+first_leg.away)>(away+first_leg.home))||
          ((home+first_leg.away)==(away+first_leg.home)&&(first_leg.away>away)))
      {
        if (home_nr>away_nr)
        {
          return 1;
        } else
        {
          return 0;
        }
      } else
      {
        if (home_nr>away_nr)
        {
          return 0;
        } else
        {
          return 1;
        }
      }
    }
  }
}

class TClub
{
  String name;
  float rank, modifier;
  int pts, w, d, l, gf, ga;
  TClub next;

  TClub( )
  {
    pts = 0;
    w = 0;
    d = 0;
    l = 0;
    gf = 0;
    ga = 0;
    modifier = 0;
  }
  public void getdata( int lfd_id )
  {
    Out.print( "Name of team "  + lfd_id + ": ");
    name = In.readLine();
    Out.print( "Rank for scorination: " );
    rank = In.readFloat();
    In.readLine();
    Out.print( "Tactical modifier: " );
    modifier = In.readFloat();
    In.readLine();
  }

  int gd()
  {
    return(gf-ga);
  }

  public void include_res(int f, int a, boolean rejis_points, boolean low_wins)
  {
    gf+=f;
    ga+=a;
    if (f>a)
    {
      pts += 3;
      w++;
      if (low_wins)
      {
        pts--;
      }
    }else
    {
      if (f==a)
      {
        pts++;
	d++;
      } else
      {
        l++;
      }
    }
    if (rejis_points)
    {
      pts++;
    }    
  }

  public int worse(TClub other) //(read as: is club worse than (otherclub))  >0 if yes, <0 if no, =0 if equal
  {
    if (pts!=other.pts)
    {
      return(other.pts-pts);
    } else
    {
      if (gd()!=other.gd())
      {
        return(other.gd()-gd());
      } else
      {
        if (gf!=other.gf)
	{
	  return(other.gf-gf);
	} else
	{
	  if (other.w!=w)
	  {
	    return(other.w-w);
	  } else
	  {
	    return(0);
	  }
	}
      }
    }
  }

  public void become(TClub idol)
  {
    pts = idol.pts;
    w = idol.w;
    d = idol.d;
    l = idol.l;
    gf = idol.gf;
    ga = idol.ga;
    name = idol.name;
    rank = idol.rank;
  }
}

class TLeague
{
  TClub first, last;
  String name;
  int regions, teams, i, j, nr_ass, md, maxlength, simTo, offset, defense, PK_defense, attack;
  TFixture MD1, matchdays;
  double expo, scale;
  boolean[] assigned;
  boolean no_draws, rejis_points, low_wins, auto_fixtures, home_away, filing, home_adv, force_MDs, eliminate, AOA, KPB;
  TMatch first_match;

  TLeague()
  {
    TClub current;
    String s;

    defense=892;
    PK_defense=400;
    expo=1;
    scale=1;
    attack=1000;
    home_adv = false;
    KPB=false;
    low_wins=false;
    Out.print ( "Number of regions (type 1 if your league is not divided in regions): " );
    regions = In.readInt();
    In.readLine();
    Out.print( "Number of teams per region: " );
    teams = In.readInt();
    In.readLine();
    first = new TClub();
    current = first;
    maxlength = 9; //"Region xx"
    for (i=0; i<regions; i++)
    {
      for (j=0; j<teams; j++)
      {
        current.getdata( j+1 );
	if (current.name.length()+2>maxlength)
	{
	  maxlength = current.name.length()+2;
	}
	current.next = new TClub();
	current = current.next;
      }
    }
    Out.print( "Allow Draws (y/n): " );
    s=In.readLine();
    no_draws=!(s.charAt(0)=='y'||s.charAt(0)=='Y');
    Out.print( "Rejistanian points (y/n): " );
    s=In.readLine();
    rejis_points=(s.charAt(0)=='y'||s.charAt(0)=='Y');
    Out.print("Shall I use auto-created fixtures (y/n): ");
    s = In.readLine();
    if (s.charAt(0)=='y'||s.charAt(0)=='Y')
    {
      auto_fixtures=true;
      Out.println("There are several types of legs: ");
      Out.println("1) Normal league with home and away leg");
      Out.println("2) One leg on neutral ground");
      if (can_be_elim_style())
      {
        Out.println("3) World Cup Style Elimination");
        Out.println("4) CL Style Elimination");
      }
      Out.print("Which is used in your league (enter the number): ");
      s = In.readLine();
      if (s.charAt(0)=='1'||s.charAt(0)=='4')
      {
        home_away=true;
      }else
      {
        home_away=false;
      }
      if (s.charAt(0)=='3'||s.charAt(0)=='4')
      {
        eliminate=true;
        fixturize(teams);
      }else
      {
        eliminate=false;
        fixturize();
      }
      if (s.charAt(0)==3)
      {
        no_draws=true;
      }
    }else
    {
      auto_fixtures=false;
      get_custom_fixtures(true);
    }    
    Out.print("Output to file (y/n): ");
    s=In.readLine();
    filing=(s.charAt(0)=='y'||s.charAt(0)=='Y');
    Out.print("Sim to match (0 for entire season): ");
    simTo=In.readInt();
    In.readLine();
    if (simTo==0)
    {
      simTo=2147483647;
    }
  }

  TLeague( String filename ) throws EFileError
  {
    String s;
    boolean changed, finalized, resuming, use_modifiers;
    int i;
    
    home_adv=false;
    In.open(filename);
    s=In.readLine();
    finalized=false;
    resuming=false;
    use_modifiers=false;
    simTo=0;
    defense=892;
    PK_defense=400;
    expo=1;
    scale=1;
    attack=1000;
    KPB=false;
    while (!finalized) //get basic data
    {
      changed=false;
      if (s.charAt(0)=='=')
      {
        if (s.startsWith("=regions"))
        {
          regions=valuenate(s, regions);
          changed=true;
        }
        if (s.startsWith("=teams"))
        {
          teams=valuenate(s, teams);
          changed=true;
        }
        if (s.startsWith("=simto")) //sim to match <number>
        {
          simTo=valuenate(s, simTo);
          changed=true;
        }
        if (s.startsWith("=nodraws"))
        {
          no_draws=true;
          changed=true;
        }
        if (s.startsWith("=rejistanianmode"))
        {
          rejis_points=true;
          changed=true;
        }
        if (s.startsWith("=lowwins"))
        {
          low_wins=true;
          changed=true;
        }                                    
        if (s.startsWith("=autofixtures"))
        {
          auto_fixtures=true;
          changed=true;
        }
        if (s.startsWith("=eliminate")&&can_be_elim_style())
        {
          eliminate=true;
          changed=true;
        }
        if (s.startsWith("=2legs")&&auto_fixtures)
        {
          home_away=true;
          changed=true;
        }        
        if (s.startsWith("=file"))
        {
          filing=true;
          changed=true;
        }
        if (s.startsWith("=resume")&&!auto_fixtures)
        {
          Out.println("Resuming session...");
          resuming=true;
          changed=true;
        }
        if (s.startsWith("=home_adv")&&!auto_fixtures)
        {
          home_adv=true;
          changed=true;
        }
        if (s.startsWith("=force_mds"))
        {
          offset=valuenate(s, offset);
          force_MDs=true;
          changed=true;
        }
        if (s.startsWith("=FHWC"))
        {
          AOA=true;
          Out.print("Scorinating high-ish");
          changed=true;
        }
        if (s.startsWith("=defense"))
        {
          changed=true;
          defense=valuenate(s, 0);
        }
        if (s.startsWith("=attack"))
        {
          changed=true;
          attack=valuenate(s, 0);
        }
        if (s.startsWith("=modifiers"))
        {
          changed=true;
          use_modifiers=true;
        }
        if (s.startsWith("=scale"))
        {
          changed=true;
          scale=valuenate(s, 0);
        }
        if (s.startsWith("=pk_defense"))
        {
          changed=true;
          PK_defense=valuenate(s, 0);
        }
        if (s.startsWith("=KPB"))
        {
          changed=true;
          expo=0.5;
          attack=660;
          defense=580;
          scale=12;
          PK_defense=400;
          KPB=true;
          Out.println("KPBing");
        }        
        if (!changed)
        {
          Out.println("Unknown Identifier: "+s);
        }        
      }
      s=In.readLine();
      finalized=s.startsWith("/");
    }
    if (!auto_fixtures)
    {
      get_custom_fixtures(false);
    }else
    {
      if (!eliminate)
      {
        fixturize();
      }else
      {
        fixturize(teams); //isn't overloading fun? ;)
      }
    }
    first=new TClub();
    last=first;
    if (simTo==0)
    {
      simTo=2147483647; //if someone exceedes that, I'll kill him/her with my bare hands!!!
    }
    maxlength = 9;
    for(i=0;i<regions*teams;i++)
    {
      last.rank=In.readFloat();
      if (use_modifiers)
      {
        last.modifier=In.readFloat();
      }
      if (resuming)
      {
        last.pts=In.readInt();
        last.w=In.readInt();
        last.d=In.readInt();
        last.l=In.readInt();
        last.gf=In.readInt();
        last.ga=In.readInt();
      }
      last.name=In.readLine();
      last.name=last.name.substring(1, last.name.length());
      if (eliminate&&i%2==0)
      {
        make_TMatch(i, last);
      }
      if (last.name.length()+2>maxlength)
      {
        maxlength=last.name.length()+2;
      }
      if (!In.done())
      {
        Out.println("Error in the input file!");
        throw new EFileError();
      }
      last.next=new TClub();
      last=last.next;
    }    
    In.close();
  }
  
  void make_TMatch(int nr, TClub result)
  {
    TMatch cur;
    
    cur = new TMatch();
    cur.scorination_level=0;
    cur.home_nr=nr+1;
    cur.away_nr=nr;
    cur.home=last.ga;
    cur.away=last.gf;
    cur.next=first_match;
    first_match=cur;
  }
  
  boolean can_be_elim_style()
  {
    return ((regions==1)&&(teams==4||teams==8||teams==16||teams==32||teams==64||teams==128||teams==256));
  }
  
  void fixturize(int nr)
  {
    TFixture cur, prev;
    int i;
    
    MD1=new TFixture();
    cur=MD1;
    prev=MD1;
    for (i=0;i<nr;i=i+2)
    {
      cur.home=i;
      cur.away=i+1;
      cur.next=new TFixture();
      prev=cur;
      cur=cur.next;
    }
    prev.next=null;
    auto_fixtures=true;
    if (home_away)
    {
      create_second_leg();
    }
  }
  
  void fixturize()
  {
    int[][] table;
    int i,j,g,t;
    TFixture cur, prev, temp;
    boolean odd_league;
    
    MD1=new TFixture();
    cur=MD1;
    prev=null;
    odd_league=teams%2==1;
    if (odd_league)
    {
      teams++;
    }
    table=new int[teams][teams-1];
    for(t=0;t<teams-1;t++)
    {
      table[t][t]=teams-1;
      table[teams-1][t]=t;
      for(g=0,i=t,j=t; g<(teams-2)/2;g++)
      {
        i++;
        if (i==teams-1)
        {
          i=0;
        }
        j--;
        if (j==-1)
        {
          j=teams-2;
        }
        table[i][t]=j;
        table[j][t]=i;
      }
    }
    for(i=0;i<teams-1;i++)
    {
      for(j=0;j<teams;j++)
      {
        if(j<table[j][i])
        {
          if(!odd_league)
          {
            cur.home_region=-1;
            cur.away_region=-1;
            cur.home=j;
            cur.away=table[j][i];
            cur.next=new TFixture();
            prev=cur;
            cur=cur.next;
          }else
          {
            if (table[j][i]!=teams-1)
            {
              cur.home_region=-1;
              cur.away_region=-1;
              cur.home=j;
              cur.away=table[j][i];
              cur.next=new TFixture();
              if (i+j%2==1)
              {
                cur = inverse(cur);
              }
              prev=cur;
              cur=cur.next;
            }
          }
        }
      }
    }
    if (odd_league)
    {
      teams--;
    }
    prev.next=null;
    if (home_away)
    {
      create_second_leg();
    }
  }
  
  void create_second_leg()
  {
    TFixture cur, first, run, prev;
    
    first=new TFixture();
    cur=first;
    prev=MD1;
    for (run=MD1;run!=null;run=run.next)
    {
      cur.next=new TFixture();
      cur=cur.next;
      cur.home_region=run.away_region;
      cur.away_region=run.home_region;
      cur.home=run.away;
      cur.away=run.home;
      prev=run;
    }
    prev.next=first.next;
  }

  void get_custom_fixtures(boolean interactive)
  {
    boolean finalized, changed;
    TFixture current, prev, temp;
    String s;
    
    MD1=new TFixture();
    prev=null;
    current=MD1;
    do
    {
      if (interactive)
      {
        Out.print("Please enter your fixture in the format: 'region1 team1 region2 team2'. End with '-5'");        
      }
      s=In.readWord();
      if (s.startsWith("="))
      {
        changed=false;
        finalized=false;
        if (s.startsWith("=autofixtures"))
        {
          changed=true;
          force_MDs=true;
          temp=MD1;
          fixturize();
          if (prev!=null)
          {
            prev.next=MD1;
            MD1=temp;
          }
          current=MD1;
          while (current.next!=null)
          {
            current=current.next;
          }
        }
        if (!changed&&s.startsWith("=2legs"))
        {
          changed=true;
          temp=MD1;
          force_MDs=true;
          home_away=true;
          fixturize();
          if (prev!=null)
          {
            prev.next=MD1;
            MD1=temp;
          }
          current=MD1;
          while (current.next!=null)
          {
            current=current.next;
          }
        }
        if (!changed&&s.startsWith("=sort"))
        {
          changed=true;
          current.home_region=-5;
        }
        s=In.readLine();
      }else
      {
        if (current==null)
        {
          Out.println("Slani!");
        }
        current.home_region=Integer.parseInt(s)-1;
        finalized=current.home_region==-6;
        if (!finalized)
        {
          current.home=In.readInt()-1;
          current.away_region=In.readInt()-1;
          current.away=In.readInt()-1;
          In.readLine();
        }
      }
      current.next=new TFixture();
      prev=current;
      current=current.next;
    }while(!finalized);
    if (!(prev==null))
    {
      prev.next=null;
    }
    In.readLine();
  }
  
  int valuenate(String s, int prev)
  {
    int result;
    if (prev==0)
    {
      result=s.indexOf(" ");
      result=Integer.parseInt(s.substring(result+1, s.length()));
    }else
    {
      result=prev;
      Out.println("Duplicate statement: "+s);
    }
    return(result);
  }

  TClub lookup(int nr)
  {
    int i;
    TClub cur;

    if (nr>(teams*regions)-1)
    {
      return( null );
    }
    cur = first;
    for(i=0;i<nr;i++)
    {
      cur=cur.next;
    }
    return(cur);
  }
  
  TMatch get_first_leg(int home, int away)
  {
    TMatch cur, result;
    
    /*DOESN'T WORK WITH >1 REGIONS*/
    /*maybe even with 1...*/
    result=null;
    cur=first_match;
    while (result==null&&cur!=null)
    {
      if (cur.home_nr==home&&cur.away_nr==away)
      {
        result=cur;
      }
      cur=cur.next;
    }
    return result;
  }

  double make_adv(double hr, double ar)
  {
    double adv;
    if(!KPB)
    {
      adv=10;
    }else
    {
      adv=3*( Math.pow(hr, expo) - Math.pow(ar, expo));
    }
    return adv;
  }

  double attack(double hr, double ar, double adv, double defense, double attack)
  {
    double result;
    if(!KPB)
    {
      result=(Math.random() * attack - defense + adv + (scale * ( Math.pow(ar, expo) - Math.pow(hr, expo))));
    }else
    {
      result=(Math.random() * attack - defense + adv + (scale * ( Math.pow(hr, expo) - Math.pow(ar, expo))));
    }
    return result;
  }

  TMatch scorinate_match(int first, int second, boolean ha )
  {
    TClub ht, at;
    int home, away, i, max;
    double t, adv, hetaki, divensi;
    boolean finalized, require_ET;
    TMatch result, first_leg;

    result = new TMatch();
    ht=lookup(first);
    at=lookup(second);
    first_leg=null;
    if (eliminate&&(home_away||home_adv)&&first<second)
    {
      first_leg=get_first_leg(second, first);
      result.first_leg=first_leg;
    }
    result.home_team = ht.name;
    result.away_team = at.name;
    result.home_region = (int)(first/teams);
    result.away_region = (int)(second/teams);
    result.home_nr = first%teams;
    result.away_nr = second%teams;
    
    home=0;
    away=0;
    if (ha)
    {
      adv=make_adv(ht.rank, at.rank);
    }else
    {
      adv=0;
    }
    max=10;
    if (AOA)
    {
      max=30;
    }
    for (i=0; i<max; i++)  //ten attacks
    {      
      hetaki=attack-ht.modifier;
      divensi=defense+at.modifier;
      if (attack(ht.rank, at.rank, adv, divensi, hetaki)>0)
      {   //formula subject to change
        home++; //increase score for the home team
      }
      hetaki=attack-at.modifier;
      divensi=defense+ht.modifier;
      if (attack(at.rank, ht.rank, 0, divensi, hetaki)>0)
      {
        away++;  //...for the away team
      }
    }
    result.scorination_level++;
    result.h_regtime = home;
    result.a_regtime = away;
    require_ET=false;
    if (first_leg!=null)
    {
      if (result.a_regtime+first_leg.home==result.h_regtime+first_leg.away)
      {
        if (result.a_regtime==first_leg.away)
        {
          require_ET=true;
        }
      }
    }
    if ((home==away)&&(no_draws)||require_ET) // Extra time
    {
      for (i=0; i<4; i++)  //Four attacks
      {
        hetaki=attack-ht.modifier;
        divensi=defense+at.modifier;
        if (attack(ht.rank, at.rank, adv, divensi, hetaki)>0)
        {   //formula subject to change
          home++; //increase score for the home team
        }
        hetaki=attack-at.modifier;
        divensi=defense+ht.modifier;
        if (attack(at.rank, ht.rank, 0, divensi, hetaki)>0)
        {
          away++;  //...for the away team
        }
      }
      result.scorination_level++;
      result.h_et = home;
      result.a_et = away;
      require_ET=(home==away);
      if (first_leg!=null&&result.home==result.h_et&&result.away==result.a_et)
      {
        require_ET=true;
      }
      if (require_ET) // penalty kicks
      {
        finalized = false;
        if (first_leg!=null)
        {
          home+=first_leg.away;
          away+=first_leg.home;
        }
        for (i=0; !(finalized); i++)
        {
          hetaki=attack-ht.modifier;
          divensi=PK_defense+at.modifier;
          if (attack(ht.rank, at.rank, adv, divensi, hetaki)>0)
          {
            home++;
          }
          hetaki=attack-at.modifier;
          divensi=PK_defense+ht.modifier;
          if (attack(at.rank, ht.rank, 0, divensi, hetaki)>0)
          {
            away++;
          }
          if (i < 5)
          {
            finalized = (Math.abs(home-away)>(4-i));
          } else
          {
            finalized = (home!=away);
          }
        }
        result.scorination_level++;
        if (first_leg!=null)
        {
          home-=first_leg.away;
          away-=first_leg.home;
        }
        result.h_pk = home;
        result.a_pk = away;
      }
    }
    result.home=home;
    result.away=away;
    ht.include_res(home, away, rejis_points, low_wins);
    at.include_res(away, home, rejis_points, low_wins);
    return result;
  }

  void yutori( TMatch prev, TMatch cur )
  {
    if (prev!=null)
    {
      prev.next = cur;
    }
  }

  TMatch scorinate( TFixture fixture, boolean ha, TMatch prev )
  {
    int i;
    TMatch cur = null;

    if ((fixture.home_region==fixture.away_region)&&(fixture.home_region==-1))
    {
      for (i=0;i<regions;i++)
      {
        cur = scorinate_match( i*teams+fixture.home, i*teams+fixture.away, ha );
        yutori( prev, cur );
        prev = cur;
      }
    } else
    {
      if ((fixture.home_region==-1))
      {
        for (i=0;i<regions;i++)
        {
          cur = scorinate_match( i*teams+fixture.home, fixture.away_region*teams+fixture.away, ha );
          yutori( prev, cur );
          prev = cur;
	}
      } else
      {
        if ((fixture.away_region==-1))
        {
          for (i=0;i<regions;i++)
          {
            cur = scorinate_match( fixture.home_region*teams+fixture.home, i*teams+fixture.away, ha );
            yutori( prev, cur );
            prev = cur;
	  }
        } else
	{
	  cur = scorinate_match( fixture.home_region*teams+fixture.home, fixture.away_region*teams+fixture.away, ha );
	  yutori( prev, cur );
          prev = cur;
	}
      }
    }
    return cur;
  }

  void sort_teams(TClub current)
  {
    int i, j;
    TClub run, best, temp;
    boolean replace;
    temp=new TClub();
    for(i=0;i<teams-1;i++,current=current.next)
    {
      for(j=i,replace=false,best=current,run=current;j<teams;j++,run=run.next)
      {
        if (best.worse(run)>0)
	{
	  best=run;
	  replace=true;
	}
      }
      if (replace)
      {
        temp.become(best);
	best.become(current);
	current.become(temp);
      }
    }
  }

  void wnl(int nr)
  {
    if(nr<-9||nr>99)
    {
      Out.print(nr);
    }else
    {
      if(nr>9)
      {
        Out.print(" "+nr);
      }else
      {
        if (nr<0)
	{
	  Out.print(" "+nr);
	}else
        {
	  Out.print("  "+nr);
	}
      }
    }
  }

  void wn(int nr)
  {
    if((nr<10)&&(nr>-1))
    {
      Out.print(" "+nr);
    }else
    {
      Out.print(nr);
    }
  }

  void output_table(TClub run, int count)
  {
    int i, j;
    TClub previous;

    if (regions<2)
    {
      i=0;
    }else
    {
      Out.print("Region " + (count+1));
      if(count+1<10)
      {
        i=8;
      }else
      {
        i=9;
      }
    }
    for (;i<maxlength+4;i++)
    {
      Out.print(" ");
    }
    Out.println("Pts  W   D   L  GF:GA   GD");
    for(i=0,previous=null;i<teams;i++,run=run.next)
    {
      if(!(previous==null))
      {
        if (run.worse(previous)>0)
	{
	  Out.print(i+1);
	  Out.print(".");
	  if (i+1<10)
	  {
	    Out.print(" ");
	  }
	}else
	{
	  Out.print("   ");
	}
      }else
      {
        Out.print("1. ");
      }
      Out.print(" "+run.name);
      for (j=run.name.length();j<maxlength-1;j++)
      {
        Out.print( " " );
      }
      wnl(run.pts);
      Out.print("  ");
      wn(run.w);
      Out.print("  ");
      wn(run.d);
      Out.print("  ");
      wn(run.l);
      if (run.gf<100)
      {
        if (run.gf>9)
        {
          Out.print("  ");
        } else
        {
          Out.print("   ");
        }
      } else
      {
        Out.print(" ");
      }
      Out.print(run.gf);
      Out.print(":");      
      Out.print(run.ga);
      if (run.ga<100)
      {
        if (run.ga>9)
        {
          Out.print("  ");
        } else
        {
          Out.print("   ");
        }
      } else
      {
        Out.print(" ");
      }
      wnl(run.gd());
      Out.println();
      previous = run;
    }
  }

  void make_tables()
  {
    int i;
    TClub from;
    for(i=0;i<regions;i++)
    {
      from = lookup(i*teams);
      sort_teams(from);
      from = lookup(i*teams);
      output_table(from, i);
    }
  }

  TFixture inverse(TFixture source)
  {
    TFixture target;

    target=new TFixture();
    target.home=source.away;
    target.away=source.home;
    target.assigned=false;
    target.home_region=source.away;
    target.away_region=source.home;
    target.next=source.next;
    return(target);
  }

  void make_new_state()
  {
    TClub run;
    int i;
    
    eliminate_losers(false);
    Out.println("=regions 1"); //FIXED!!!
    Out.println("=teams "+teams);
    Out.println("=autofixtures ");
    if (no_draws)
    {
      Out.println("=nodraws ");
    }
    if (filing)
    {
      Out.println("=file ");
    }
    if (home_away)
    {
      Out.println("=2legs ");
    }
    Out.println("=eliminate ");
    Out.println("/");
    for (run=first,i=0; i<teams;run=run.next,i++)
    {
      Out.println(run.rank+" "+run.name);
    }
  }

  void saveState(TFixture current, int count)
  {
    TClub run;
    int i;
    
    Out.open("leaguedef.resume.lgn");
    if ((eliminate&&home_away&&current.next==null&&count==teams-1)
        ||(eliminate&&!home_away&&current.next==null))
    {
      make_new_state();
      Out.close();
      return;
    }
    current=current.next;
    Out.println("=regions "+regions);
    Out.println("=teams "+teams);
    Out.println("=resume");
    if (no_draws)
    {
      Out.println("=nodraws ");
    }
    if (rejis_points)
    {
      Out.println("=rejistanianmode ");
    }
    if (filing)
    {
      Out.println("=file ");
    }
    if (home_away)
    {
      Out.println("=home_adv ");
    }
    if (force_MDs)
    {
      Out.println("=force_mds "+(offset+(count+1)/(teams/2)));
    }
    if (eliminate)
    {
      Out.println("=eliminate ");
    }
    Out.println("=modifiers ");
    Out.println("/");
    for (;current!=null;current=current.next)
    {
      Out.println((current.home_region+1)+" "+(current.home+1)+" "+(current.away_region+1)+" "+(current.away+1));
    }
    Out.println("-5");
    for (run=first;run.next!=null;run=run.next)
    {
      Out.println(run.rank+" "+run.modifier+" "+run.pts+" "+run.w+" "+run.d+" "+run.l+" "+run.gf+" "+run.ga+" "+run.name);
    }
    Out.close();
  }
  
  void sort_and_say_results( TMatch first )
  {
    TMatch cur, temp, run, wert;
    TQueue res_list[];
    int i, j, n=1;
    
    cur=first;
    wert = first;
    if (auto_fixtures&&regions>1)
    {
      cur = first.next;
      run = wert;
      while (cur!=null)
      {
        n++;
        res_list = new TQueue[regions];
        for (i=0; i<regions; i++)
        {
          res_list[i] = new TQueue();
        }
        for (i=0;i<(regions*((int)(teams/2)))&&cur!=null;i++, cur=cur.next)
        {
          res_list[cur.home_region].enqueue(cur);
        }
        for (i=0; i<regions; i++)
        {
          while(res_list[i].front()!=null)
          { 
            run.next = (TMatch)(res_list[i].front());
            run = run.next;
            res_list[i].dequeue();
          }
        }
        if (cur==null)
        {
          run.next = null;
        }
      }
    }
    if (force_MDs||home_away)
    {
      cur=wert;
      j=0;
      while (cur!=null)
      {
        j++;
        temp=cur.next;
        cur.next=new TMatch();
        cur.next.next=temp;
        cur.next.home_team="Matchday "+j;
        cur=temp;
        for (i=1;i<((int)(teams/2)*regions)&&cur!=null;i++)
        {
          cur=cur.next;
        }
      }
    }
    if (eliminate)
    {
      Out.println("Round of "+teams);
    }
    for ( cur = first; cur!=null; cur=cur.next )
    {
      if (cur.scorination_level>0)
      {
        Out.print(cur.home_team + " " + cur.h_regtime + " " + cur.away_team + " " + cur.a_regtime);
        if (cur.scorination_level>1)
        {
          Out.print( "  AET: " + cur.h_et + ":" + cur.a_et );
          if (cur.scorination_level>2 )
          {
            Out.print( "  APK: " + cur.h_pk + ":" + cur.a_pk );
          }
        }
        Out.println();
      }else
      {
        if (cur.next!=null&&!eliminate)
        {
          Out.println(cur.home_team);
        }
      }
    }
  }

  void eliminate_losers(boolean cont)
  {
    TMatch cur;
    int i;
    TClub run, prev;
    
    if (teams==2)
    {
      return;
    }
    run=first;
    cur=first_match;
    prev=null;
    if (home_away)
    {
      while (cur.first_leg==null)
      {
        cur=cur.next;
      }
    }        
    for (;cur!=null;cur=cur.next)
    {
      if (cur.scorination_level!=0)
      {
        if (cur.winner()==1)
        {
          if (run!=first)
          {
            prev.next=run.next;
          } else
          {
            first=first.next;
          }
          prev=run;
          run=run.next;
        } else
        {
          if (run.next!=null)
          {
            run.next=run.next.next;
          }
        }
        if (run!=null)
        {
          prev=run;
          run=run.next;
        }
      }
    }
    teams = (int)(teams/2);
    if((teams>1)&&cont)
    {
/*      if (home_adv)
      {
        home_adv=false;
        home_away=true;
      }*/
      fixturize(teams);
      first_match = new TMatch();
      first_match.home_team = "Scorination by leagion";
      simulate_season();
    }
  }

  public void simulate_season()
  {
    TFixture current, last;
    String s;
    int i;
    TMatch cur_match, showed_scores_to;

    last=MD1;
    cur_match=first_match;
    showed_scores_to=first_match;
    while(cur_match.next!=null)
    {
      cur_match=cur_match.next;
    }
    for(current=MD1, i=0; current!=null&&simTo>0; current=current.next, i++)
    {
      if (current.home_region>-5)
      {
        cur_match = scorinate(current, home_adv, cur_match);
        simTo--;
        last=current;
      } else
      {
        if (current.home_region==-5)
        {
          sort_and_say_results(showed_scores_to);
          make_tables();
          cur_match.next=new TMatch();
          cur_match=cur_match.next;
          cur_match.home_team="";
          showed_scores_to=cur_match;
          home_adv=false;
          home_away=false;
          force_MDs=false;
        }
        last=current;
      }
    }
    if (cur_match!=showed_scores_to)
    {
      sort_and_say_results( showed_scores_to );
    }
    if (simTo==0)
    {
      saveState(last, i-1);
    }
    if (!eliminate)
    {
      if (cur_match!=showed_scores_to)
      {
        make_tables();
      }
    } else
    {
      if (simTo!=0)
      {
        eliminate_losers(true);
      }
    }
  }
  
  public void simulate()
  {
    if (filing)
    {
      Out.open("league.txt");
    }
    if (first_match==null)
    {
      first_match = new TMatch();
      first_match.home_team = "Scorination by leagion";
    }
    simulate_season();
    if (filing)
    {
      Out.close();
    }
  }
}

class leagion
{
  public static void main (String args[]) throws EFileError
  {
    TLeague league;
    
    try
    {
      In.open(args[0]);
      if (!In.done()){throw new EFileError();}
      In.close();
      try
      {
        league = new TLeague( args[0] );
      }catch(Exception e)
      {
        Out.println("Oh slani!");
        e.printStackTrace();
        return;
      }
    } catch (Exception e)
    {
      league = new TLeague( );
    }  
    league.simulate();
  }
}

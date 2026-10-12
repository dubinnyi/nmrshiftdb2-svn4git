/*
 * Copyright 2010 Stefan Kuhn
 * 
 * This file is part of nmrshiftdb2.
 *
 * nmrshiftdb2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * nmrshiftdb2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with nmrshiftdb2.  If not, see <http://www.gnu.org/licenses/>.
 */
#include <iostream>
#include <string>
#include <vector>
#include <cmath>

#ifdef STANDARD
#include <stdio.h>
#include <string.h>
#else
#include <my_global.h>
#include <my_sys.h>
#endif
#include <mysql.h>
#include <m_ctype.h>
#include <m_string.h>

#ifdef HAVE_DLOPEN

using namespace std;

/*
This calculates the similarity between two spectra given as SpecFile
The calculation is the same as in DBSpectrum.java. See this for comments
*/
vector<pair<double,pair<double,int> > > stringToVector(char *x, bool checkDoubleEntries){
	vector<pair<double,pair<double,int> > > v;
	bool isReadingShift=true;
	bool isReadingIntensity=false;
	string shift;
	string intensity;
	for(int i=0;i<strlen(x);i++){
		if(x[i]!=';' && x[i]!=',' && x[i]!='|'){
			if(isReadingShift)
				shift.append(1,x[i]);
			else
				if(isReadingIntensity)
					intensity.append(1,x[i]);
		}
		else
		{
			if(x[i]==';' && isReadingIntensity){
				isReadingIntensity=false;
			}
			if(x[i]==';' && isReadingShift){
				isReadingShift=false;
				isReadingIntensity=true;
			}
			if(x[i]=='|'){
				char* pEnd;
				isReadingShift=true;
				double dblshift= strtod(shift.c_str(),&pEnd);
        bool alreadyIn=false;
				if(checkDoubleEntries){
					for(int i=0;i<v.size();i++)
						if(v[i].first==dblshift)
							alreadyIn=true;
				}
        int multiplicity=-1;
        double dblintensity;
        if(intensity[intensity.size()-1]=='S'){
          multiplicity=0;
          dblintensity=strtod(intensity.substr(0,intensity.size()-1).c_str(),&pEnd);
        } else if(intensity[intensity.size()-1]=='D'){
          multiplicity=1;
          dblintensity=strtod(intensity.substr(0,intensity.size()-1).c_str(),&pEnd);
        } else if(intensity[intensity.size()-1]=='T'){
          multiplicity=2;
          dblintensity=strtod(intensity.substr(0,intensity.size()-1).c_str(),&pEnd);
        } else if(intensity[intensity.size()-1]=='Q'){
          multiplicity=3;
          dblintensity=strtod(intensity.substr(0,intensity.size()-1).c_str(),&pEnd);
        } else {
          dblintensity=strtod(intensity.c_str(),&pEnd);
        }
				pair<double,pair<double,int> > p(dblshift,pair<double,int>(dblintensity,multiplicity));
        v.push_back(p);
				shift="";
				intensity="";
			}
		}
	}
	return(v);
} 

double similarity(char *x, char *y, char *isSubSpectrum){
	vector<pair<double,pair<double,int> > > v1=stringToVector(x,false);
	vector<pair<double,pair<double,int> > > v2=stringToVector(y,false);
	vector<pair<double,pair<double,int> > >* largerVector;
	vector<pair<double,pair<double,int> > >* smallerVector;
	double similarityPerSignal=0.0;	
	char subspectrum[] = "subspectrum";
	bool smallervectorisoriginal = true;
	if(strcmp (isSubSpectrum,subspectrum) == 0){
		smallerVector=&v2;
		largerVector=&v1;
		similarityPerSignal = 100.0 / smallerVector->size();
	}
	else
	{
	if(v1.size()>v2.size()){
		largerVector=&v1;
		smallerVector=&v2;
	}
	else
	{
		smallervectorisoriginal=false;
	      largerVector=&v2;
	      smallerVector=&v1;
	}
	similarityPerSignal = 100.0/largerVector->size();
	}
	bool signalsUsed[largerVector->size()];
	for(int  i=0;i<largerVector->size();i++){
		signalsUsed[i]=false;
	}
	double similarity = 0;
	for(int i=0; i < smallerVector->size(); i++){
		int nearestSignal=0;
		double difference= 32767;
		for (int k=0; k< largerVector->size();k++){
			if(fabs((*largerVector)[k].first-(*smallerVector)[i].first) < difference && 
					signalsUsed[k]==false  && 
					((smallervectorisoriginal && ((*smallerVector)[i].second.second==-1 || ((*largerVector)[k].second.second!=-1 && (*smallerVector)[i].second.second==(*largerVector)[k].second.second))||
					(!smallervectorisoriginal && ((*largerVector)[i].second.second==-1 || (*smallerVector)[i].second.second!=-1 && (*smallerVector)[i].second.second==(*largerVector)[k].second.second))))){
				difference=fabs((*largerVector)[k].first-(*smallerVector)[i].first);
				nearestSignal=k;
			}
		}
                if(difference<similarityPerSignal){
			if((*smallerVector)[i].second.first != 0 && (*largerVector)[nearestSignal].second.first !=0)
				similarity += (similarityPerSignal-difference)*(((*smallerVector)[i].second.first<(*largerVector)[nearestSignal].second.first ? (*smallerVector)[i].second.first : (*largerVector)[nearestSignal].second.first )/((*smallerVector)[i].second.first>(*largerVector)[nearestSignal].second.first ? (*smallerVector)[i].second.first : (*largerVector)[nearestSignal].second.first ));
			else
				similarity += similarityPerSignal -difference;
		}
		signalsUsed[nearestSignal]=true;
	}
	return(similarity);
}

double similarity2d(char *x, char *y, char *isSubSpectrum){
	vector<pair<double,pair<double,int> > > v1=stringToVector(x,false);
	vector<pair<double,pair<double,int> > > v2=stringToVector(y,false);
	vector<pair<double,pair<double,int> > >* largerVector;
	vector<pair<double,pair<double,int> > >* smallerVector;
	double similarityPerSignal=0.0;	
	char subspectrum[] = "subspectrum";
	bool smallervectorisoriginal = true;
	if(strcmp (isSubSpectrum,subspectrum) == 0){
		smallerVector=&v2;
		largerVector=&v1;
		similarityPerSignal = 100.0 / smallerVector->size();
	}
	else
	{
	if(v1.size()>v2.size()){
		largerVector=&v1;
		smallerVector=&v2;
	}
	else
	{
		smallervectorisoriginal=false;
	      largerVector=&v2;
	      smallerVector=&v1;
	}
	similarityPerSignal = 100.0/largerVector->size();
	}
	bool signalsUsed[largerVector->size()];
	for(int  i=0;i<largerVector->size();i++){
		signalsUsed[i]=false;
	}
	double similarity = 0;
	for(int i=0; i < smallerVector->size(); i++){
		int nearestSignal=0;
		double difference= 32767;
		for (int k=0; k< largerVector->size();k++){
			cout << fabs((*largerVector)[k].first-(*smallerVector)[i].first)/20+fabs((*largerVector)[k].second.first-(*smallerVector)[i].second.first) << endl;
			if(fabs((*largerVector)[k].first-(*smallerVector)[i].first)/20+fabs((*largerVector)[k].second.first-(*smallerVector)[i].second.first) < difference && 
					signalsUsed[k]==false  ){
				difference=fabs((*largerVector)[k].first-(*smallerVector)[i].first)/20+fabs((*largerVector)[k].second.first-(*smallerVector)[i].second.first);
				nearestSignal=k;
			}
		}
		similarity += similarityPerSignal -difference;
		signalsUsed[nearestSignal]=true;
	}
	return(similarity);
}

extern "C"
{
  my_bool spectrumsimilarity_init(UDF_INIT *initid, UDF_ARGS *args, char *message);
  double spectrumsimilarity( UDF_INIT* initid, UDF_ARGS* args, char *is_null, char* error);
  my_bool spectrumsimilarity2d_init(UDF_INIT *initid, UDF_ARGS *args, char *message);
  double spectrumsimilarity2d( UDF_INIT* initid, UDF_ARGS* args, char *is_null, char* error);
}

my_bool spectrumsimilarity_init(UDF_INIT *initid, UDF_ARGS *args, char *message){
	return 0;
}

my_bool spectrumsimilarity2d_init(UDF_INIT *initid, UDF_ARGS *args, char *message){
	return 0;
}

double spectrumsimilarity( UDF_INIT* initid, UDF_ARGS* args, char *is_null, char* error)
{
  char x[1000];
  char y[1000];
  char isSubSpectrum[5];

  bzero(x, 1000);
  bzero(y, 1000);
  bzero(isSubSpectrum, 5);

  strncpy( x, args->args[0], args->lengths[0]>999 ? 999 : args->lengths[0]);
  strncpy( y, args->args[1], args->lengths[1]>999 ? 999 : args->lengths[1]);
  strncpy( isSubSpectrum, args->args[2], args->lengths[2] );

  return similarity(x,y,isSubSpectrum);
}

double spectrumsimilarity2d( UDF_INIT* initid, UDF_ARGS* args, char *is_null, char* error)
{
  char x[1000];
  char y[1000];
  char isSubSpectrum[5];

  bzero(x, 1000);
  bzero(y, 1000);
  bzero(isSubSpectrum, 5);

  strncpy( x, args->args[0], args->lengths[0]>999 ? 999 : args->lengths[0]);
  strncpy( y, args->args[1], args->lengths[1]>999 ? 999 : args->lengths[1]);
  strncpy( isSubSpectrum, args->args[2], args->lengths[2] );

  return similarity2d(x,y,isSubSpectrum);
}
#endif /* HAVE_DLOPEN */

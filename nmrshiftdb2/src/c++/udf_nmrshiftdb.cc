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

/* These must be right or mysqld will not find the symbol! */

extern "C"
{
  my_bool fingerprint_compare_init(UDF_INIT *initid, UDF_ARGS *args, char *message);
  char *fingerprint_compare( UDF_INIT* initid, UDF_ARGS* args, char *result, unsigned long *length, char *is_null, char *error );
  // http://dev.mysql.com/doc/refman/5.0/en/udf-security.html
  void fingerprint_compare_deinit(UDF_INIT *initid) {};
  char *fingerprint_compare_reset(UDF_INIT *initid, UDF_ARGS *args, char *is_null, char *error) {};
  char *fingerprint_compare_clear(UDF_INIT *initid, char *is_null, char *error) {};
  char *fingerprint_compare_add(UDF_INIT *initid, UDF_ARGS *args, char *is_null, char *error) {};

  my_bool fingerprint_compare64_init(UDF_INIT *initid, UDF_ARGS *args, char *message);
  char *fingerprint_compare64( UDF_INIT* initid, UDF_ARGS* args, char *result, unsigned long *length, char *is_null, char *error );
 // http://dev.mysql.com/doc/refman/5.0/en/udf-security.html
  void fingerprint_compare64_deinit(UDF_INIT *initid) {};
  char *fingerprint_compare64_reset(UDF_INIT *initid, UDF_ARGS *args, char *is_null, char *error) {};
  char *fingerprint_compare64_clear(UDF_INIT *initid, char *is_null, char *error) {};
  char *fingerprint_compare64_add(UDF_INIT *initid, UDF_ARGS *args, char *is_null, char *error) {};

  my_bool tanimoto_coefficient_init(UDF_INIT *initid, UDF_ARGS *args, char *message);
  double tanimoto_coefficient( UDF_INIT* initid, UDF_ARGS* args, char *is_null, char* error);
 // http://dev.mysql.com/doc/refman/5.0/en/udf-security.html
  void tanimoto_coefficient_deinit(UDF_INIT *initid) {};
  char *tanimoto_coefficient_reset(UDF_INIT *initid, UDF_ARGS *args, char *is_null, char *error) {};
  char *tanimoto_coefficient_clear(UDF_INIT *initid, char *is_null, char *error) {};
  char *tanimoto_coefficient_add(UDF_INIT *initid, UDF_ARGS *args, char *is_null, char *error) {};

}

  my_bool fingerprint_compare_init(UDF_INIT *initid, UDF_ARGS *args, char *message){
    return 0;
  }
  my_bool fingerprint_compare64_init(UDF_INIT *initid, UDF_ARGS *args, char *message){
    return 0;
  }
  my_bool tanimoto_coefficient_init(UDF_INIT *initid, UDF_ARGS *args, char *message){
    return 0;
  }


char *fingerprint_compare( UDF_INIT* initid, UDF_ARGS* args, 
			   char *result, unsigned long *length,
			   char *is_null, char *error)
{
  unsigned long long twopowersixtythree=9223372036854775807LL;//2^63
  
  for(int i=0;i<16;i++)
  {
    if (args->args[i] == NULL || args->args[i+16] == NULL) {
      *is_null=1;
      return NULL;
    }

    unsigned long long firstvalue=*((unsigned long long*)args->args[i]);
    unsigned long long secondvalue=*((unsigned long long*)args->args[i+16]);
    
    if(secondvalue>=twopowersixtythree && firstvalue<twopowersixtythree)
    {
      memcpy(result, "N", 1);
      *length = 1;
      return result;
    }
    
    if(firstvalue>=twopowersixtythree)
      firstvalue=firstvalue-twopowersixtythree;
    if(secondvalue>=twopowersixtythree)
      secondvalue=secondvalue-twopowersixtythree;
    
    if((firstvalue & secondvalue) != secondvalue)
    {
      memcpy(result, "N", 1);
      *length = 1;
      return result;
    }
  }
  memcpy(result, "Y", 1);
  *length = 1;
  return result;
}

char *fingerprint_compare64( UDF_INIT* initid, UDF_ARGS* args, 
			     char *result, unsigned long *length,
			     char *is_null, char *error)
{

  if (args->args[0] == NULL || args->args[1] == NULL) {
    *is_null=1;
    return NULL;
  }

  long long fp1=*((long long*)args->args[0]);
  long long fp2=*((long long*)args->args[1]);

  if(fp2%2!=0 && fp1%2==0 && (fp1>>1)%2==0)
  {
      memcpy(result, "N", 1);
      *length = 1;
      return result;
  }
  if((fp2>>63)%2!=0 && (fp1>>62)%2==0 && (fp1>>63)%2==0)
  {
      memcpy(result, "N", 1);
      *length = 1;
      return result;
  }
  for(int i=2;i<63;i=i+2)
  {
    if((fp1>>(i-1))%2 == 0 && (fp1>>i)%2 == 0 && (fp1>>(i+1))%2 == 0 && (fp2>>i)%2 != 0)
    {
      memcpy(result, "N", 1);
      *length = 1;
      return result;
    }
  }
  memcpy(result, "Y", 1);
  *length = 1;
  return result;
}

double tanimoto_coefficient( UDF_INIT* initid, UDF_ARGS* args, char *is_null, char* error)
{
  int bitsinfirstvalue=0;
  int bitsinsecondvalue=0;
  int bitsinbothvalues=0;

  for(int i=0;i<16;i++)
  {

    if (args->args[i] == NULL || args->args[i+16] == NULL) {
      *is_null=1;
      return NULL;
      fprintf (stderr, "\n");
    }

    long long firstvalue=*((long long*)args->args[i]);
    long long secondvalue=*((long long*)args->args[i+16]);

    for(int k=0;k<64;k++)
    {
      if(firstvalue & ((long long)1 << (long long)k))
        bitsinfirstvalue++;
      if(secondvalue & ((long long)1 << (long long)k))
        bitsinsecondvalue++;
      if(firstvalue & ((long long)1 << (long long)k) && secondvalue & ((long long)1 << (long long)k))
        bitsinbothvalues++;
    }
  }
  if ((bitsinfirstvalue+bitsinsecondvalue-bitsinbothvalues) != 0) {
    return((bitsinbothvalues*100)/(bitsinfirstvalue+bitsinsecondvalue-bitsinbothvalues));
  } else {
    return(0);
  }
}

#endif /* HAVE_DLOPEN */

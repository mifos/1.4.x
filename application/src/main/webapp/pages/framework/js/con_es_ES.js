/**

 * con_ar.js    version: 1

 

 * Copyright (c) 2005-2006 Grameen Foundation USA

 * 1029 Vermont Avenue, NW, Suite 400, Washington DC 20005

 * All rights reserved.

 

 * Apache License 
 * Copyright (c) 2005-2006 Grameen Foundation USA 
 * 

 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 *

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the 

 * License. 
 * 
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an explanation of the license 

 * and how it is applied. 

 *

 */

function es_ES_validate(txt,min,max)
{
      var 	asci_txt="";
	  asci_txt=genricConvertor(txt,spanishToAsci);
      return envalidate(asci_txt,min,max);
}
function spanishToAsci(char_code)
{
       return genericMap(char_code,SPANISH.CODE_0,SPANISH.CODE_9,SPANISH.CODE_DECIMAL);
}
function es_ES_OnkeyPress(fmt,element,e)
{
	return genericOnkeyPress(fmt,element,e,SPANISH.CODE_0,SPANISH.CODE_9,SPANISH.CODE_DECIMAL);
}
function es_ESAlertRange(min,max)
{
   alert(" Valore entrado debe estar en la gama : " + min + " - " + max);
}
function es_ESAlertformat(format)
{
	alert( "Entre por favor un formato v�lido  " + format); 
}
function es_ESAlertMinNumber(min)
{
	alert( "Entre por favor atleast " +min+ " n�meros" ); 
}

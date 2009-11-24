function makeDateString(Text1,Text2,Text3,Text4,separator){
	var finalDateProperty = document.getElementById(Text4);
	if(Text1 == "" && Text2 =="" && Text3 =="") {
		finalDateProperty.value="";
	}
	else {
		var dateProperty1 = document.getElementById(Text1);
		var dateProperty2 = document.getElementById(Text2);
		var dateProperty3 = document.getElementById(Text3);				
		if(dateProperty1.value == "" && dateProperty2.value =="" && dateProperty3.value =="") {
			finalDateProperty.value="";
		}
		else{
			finalDateProperty.value=dateProperty1.value+separator+dateProperty2.value+separator+dateProperty3.value;
		}
	}
}
var bCancel = false; 

function validateMyForm(field,pat,focusField) 
{    
	 if (bCancel) 
     {
		 return true; 
	 }
	else 
	 {
		 var formValidationResult;
	 }
	 formValidationResult = validateDate(field,pat,focusField); 
     return (formValidationResult == 1);
} 

function myForm_DateValidations (pat) 
{ 
	this.a0 = new Array("dob", "Please enter a valid date", new Function ("varName", "this.datePattern='"+pat+"';  return this[varName];"));
} 
function validateDate(field,pat,field2) 
{
  var bValid = true;
     var focusField = null;
     var i = 0;
     var fields = new Array();

     oDate = eval('new myForm_DateValidations(\'' + pat.value + '\')');
   
   for (x in oDate) 
	{
	  var value = field.value;
	  datePattern=pat.value;
	
          if ((field.type == 'hidden' ||  field.type == 'text' ||  field.type == 'textarea') && (value.length > 0) && (datePattern.length > 0) && field.disabled == false) 
		{				
		 var MONTH = "M";
		 var DAY = "D";
		 var YEAR = "Y";
		 var orderMonth = datePattern.indexOf(MONTH);
		 var orderDay = datePattern.indexOf(DAY);
		 var orderYear = datePattern.indexOf(YEAR);
	   				 
		 if ((orderDay < orderYear && orderDay > orderMonth)) 
		 {
		 var iDelim1 = orderMonth + MONTH.length;
		 var iDelim2 = orderDay + DAY.length;
		 var delim1 = datePattern.substring(iDelim1, iDelim1 + 1);
		 var delim2 = datePattern.substring(iDelim2, iDelim2 + 1);
		 if (iDelim1 == orderDay && iDelim2 == orderYear) 
		 {
			dateRegexp = new RegExp("^(\\d{1,2})(\\d{1,2})(\\d{4})$");
		 } 
		 else if (iDelim1 == orderDay) 
		 {
                     dateRegexp = new RegExp("^(\\d{1,2})(\\d{1,2})[" + delim2 + "](\\d{4})$");
  			 }
		 else if (iDelim2 == orderYear) 
		 {
                     dateRegexp = new RegExp("^(\\d{1,2})[" + delim1 + "](\\d{1,2})(\\d{4})$");
		 } 
		 else 
		 {
                     dateRegexp = new RegExp("^(\\d{1,2})[" + delim1 + "](\\d{1,2})[" + delim2 + "](\\d{4})$");
		 }
		 
		 var matched = dateRegexp.exec(value);
		 if(matched != null) 
		 {
			if (!isValidDate(matched[2], matched[1], matched[3])) 
			{
				if (i == 0)
				{
                            focusField = field2;
                      }
				fields[i++] = oDate[x][1];
				bValid =  false;
			}
		}
			else 
			{
                  if (i == 0) 
				{
					focusField = field2;
                      }
                   fields[i++] = oDate[x][1];
                bValid =  false;
	        }
              }
		 
		 else if ((orderMonth < orderYear && orderMonth > orderDay)) 
		 {
            var iDelim1 = orderDay + DAY.length;
            var iDelim2 = orderMonth + MONTH.length;
            var delim1 = datePattern.substring(iDelim1, iDelim1 + 1);
            var delim2 = datePattern.substring(iDelim2, iDelim2 + 1);
            if (iDelim1 == orderMonth && iDelim2 == orderYear) {
                dateRegexp = new RegExp("^(\\d{1,2})(\\d{1,2})(\\d{4})$");
            } else if (iDelim1 == orderMonth) {
                dateRegexp = new RegExp("^(\\d{1,2})(\\d{1,2})[" + delim2 + "](\\d{4})$");
            } else if (iDelim2 == orderYear) {
                dateRegexp = new RegExp("^(\\d{1,2})[" + delim1 + "](\\d{1,2})(\\d{4})$");
            } else {
                dateRegexp = new RegExp("^(\\d{1,2})[" + delim1 + "](\\d{1,2})[" + delim2 + "](\\d{4})$");
            }
            var matched = dateRegexp.exec(value);
            if(matched != null) {
                if (!isValidDate(matched[1], matched[2], matched[3])) {
                    if (i == 0) {
                focusField = field2;
                    }
                    fields[i++] = oDate[x][1];
                    bValid =  false;
                 }
            } else {
                if (i == 0) {
                    focusField = field2;
                }
                fields[i++] = oDate[x][1];
                bValid =  false;
            }
        }
		 else if ((orderMonth > orderYear && orderMonth < orderDay)) 
		 {
		     var iDelim1 = orderYear + YEAR.length;
		     var iDelim2 = orderMonth + MONTH.length;
		     var delim1 = datePattern.substring(iDelim1, iDelim1 + 1);
		     var delim2 = datePattern.substring(iDelim2, iDelim2 + 1);
	
              if (iDelim1 == orderMonth && iDelim2 == orderDay) {
                  dateRegexp = new RegExp("^(\\d{4})(\\d{1,2})(\\d{1,2})$");
              } else if (iDelim1 == orderMonth) {
                  dateRegexp = new RegExp("^(\\d{4})(\\d{1,2})[" + delim2 + "](\\d{1,2})$");
              } else if (iDelim2 == orderDay) {
                  dateRegexp = new RegExp("^(\\d{4})[" + delim1 + "](\\d{1,2})(\\d{1,2})$");
              } else {
				  dateRegexp = new RegExp("^(\\d{4})[" + delim1 + "](\\d{1,2})[" + delim2 + "](\\d{1,2})$");
              }
			 if(value!="//")
			 {
				var matched = dateRegexp.exec(value);
				if(matched != null) {
					if (!isValidDate(matched[3], matched[2], matched[1])) 
					 {
						if (i == 0) 
						{
							 focusField = field2;
						}
						bValid =  false;
					}
				} 
				 else 
				{
					 if (i == 0) 
					{							 
						focusField = field2;
					 }
					 fields[i++] = oDate[x][1];
					bValid =  false;
				 }
			 }
               } 
		 else 
		 {
             if (i == 0) 
			 {
             focusField = field2;
             }
             fields[i++] = oDate[x][1];
             bValid =  false;
         }
	 }//END OF IF LOOP
     }//END OF FOR LOOP
     if (fields.length > 0) 
  {
        focusField.focus();
        alert(fields.join('\n'));
 
     }
     return bValid;
}
function isValidDate(day, month, year) 
	{
	    if (month < 1 || month > 12) {
            return false;
        }
		if (month==4 || month==6 || month==9 || month==11 )
		{
			if (day < 1 || day > 30) 
			{
            return false;
			 }
		}
		if (month == 1 || month == 3 || month == 5 || month == 7 || month==8 || month ==10 || month==12) 
		{
			if (day < 1 || day > 31) 
			{
            return false;
			 }
        }
         if (month == 2) 
			{
            var leap = (year % 4 == 0 &&
               (year % 100 != 0 || year % 400 == 0));
            if (day < 1 || day>29 || (day == 29 && !leap)) {
                return false;
            }
        }
		 return true;
    }
function numbersonly(myfield, e, dec)
{
var key;
var keychar;

if (window.event)
   key = window.event.keyCode;
else if (e)
   key = e.which;
else
   return true;
keychar = String.fromCharCode(key);
// control keys
if ((key==null) || (key==0) || (key==8) || 
    (key==9) || (key==27) )
   return true;
// numbers
else if ((("0123456789").indexOf(keychar) > -1))
   return true;
// decimal point jump
else if (dec && (keychar == "."))
   {
   myfield.form.elements[dec].focus();
   return false;
   }
else
   return false;
}
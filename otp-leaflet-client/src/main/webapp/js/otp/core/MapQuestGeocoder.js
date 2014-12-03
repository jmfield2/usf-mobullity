/* This program is free software: you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public License
   as published by the Free Software Foundation, either version 3 of
   the License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/


otp.namespace("otp.core");

otp.core.MapQuestGeocoder = otp.Class({
    
    url : null,
    addressParam : null,
    
    initialize : function(url, addressParam) {
        this.url = url;
        this.addressParam = addressParam;
    },
    
    geocode : function(address, setResultsCallback) {
    
        var params = {
            format: 'xml',
            countrycodes: 'SI'
        }; 
        params[this.addressParam] = address;
        
        $.ajax(this.url, {
            data : params,
            
            success: function(data) {
                if((typeof data) == "string") data = jQuery.parseXML(data);
                var results = [];
                $(data).find("searchresults").find("place").each(function () {
                    var resultXml = $(this);
                    
                    var resultObj = {
                        description : resultXml.attr("display_name"),
                        lat : resultXml.attr("lat"),
                        lng : resultXml.attr("lon")
                    };
    
                    results.push(resultObj);                    
                });
                
                setResultsCallback.call(this, results);
            }
        });        
    } 
    
});

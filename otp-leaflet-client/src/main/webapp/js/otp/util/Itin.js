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

otp.namespace("otp.util");

/**
 * Utility routines for OTP itinerary-based operations
 */
 
otp.util.Itin = {

    /** 
     * Extracts the "place" from an OTP "name::place" string, where "place" is
     * a latitude,longitude string or a vertex ID.
     *
     * @param {string} locationStr an OTP GenericLocation string
     * @return {string} the "place" component of an OTP location string 
     */
     
    getLocationPlace : function(locationStr) {
        return locationStr.indexOf("::") != -1 ? 
            locationStr.split("::")[1] : locationStr;
    },


    /** 
     * Extracts the "name" from an OTP "name::place" string, if present
     *
     * @param {string} locationStr an OTP GenericLocation string
     * @return {string} the "name" component of an OTP location string, null if not present 
     */

    getLocationName : function(locationStr) {
        return locationStr.indexOf("::") != -1 ? 
            locationStr.split("::")[0] : null;
    },
    
    isTransit : function(mode) {
        return mode === "TRANSIT" || mode === "SUBWAY" || mode === "RAIL" || mode === "BUS" || mode === "TRAM" || mode === "GONDOLA" || mode === "TRAINISH" || mode === "BUSISH";
    },
    
    includesTransit : function(mode) {
        var modeArr = mode.split(",");
        for(var i = 0; i < modeArr.length; i++) {
            if(this.isTransit(modeArr[i])) return true;
        }
        return false;
    },
    
    includesWalk : function(mode) {
        var modeArr = mode.split(",");
        for(var i = 0; i < modeArr.length; i++) {
            if(modeArr[i] === "WALK") return true;
        }
        return false;
    },

    includesBicycle : function(mode) {
        var modeArr = mode.split(",");
        for(var i = 0; i < modeArr.length; i++) {
            if(modeArr[i] === "BICYCLE") return true;
        }
        return false;
    },


    /** 
     * Returns localized relative direction string
     *
     * @param {string} dir a relative direction string from a server
     * @return {string} localized direction string
     */

    directionString : function(dir) { 
        var directionStrings= {
            'DEPART': pgettext("itinerary", "Depart"),
            //TRANSLATORS: [Relative direction (Hard/Slightly Left/Right...)] to continue
            //on /on to [streetname]
            'HARD_LEFT': _tr("Hard Left"),
            'LEFT': _tr("Left"),
            'SLIGHTLY_LEFT': _tr("Slight left"), 
            'CONTINUE': _tr("Continue"),
            'SLIGHTLY_RIGHT': _tr("Slight right"),
            'RIGHT': _tr("Right"),
            'HARD_RIGHT': _tr("Hard right"),
            'ELEVATOR': _tr("Elevator"),
            'UTURN_LEFT': _tr("U-turn left"),
            'UTURN_RIGHT': _tr("U-turn right")
        };
        if (dir in directionStrings) return directionStrings[dir];
        // This is used if dir isn't found in directionStrings
        // This shouldn't happen
        return dir.toLowerCase().replace('_',' ').replace('ly','');
    },
    
    distanceString : function(m) {
        return otp.util.Geo.distanceString(m);
    },
    
    modeStrings : {
        //TRANSLATORS: Walk distance to place (itinerary header)
        'WALK': _tr('Walk'),
        //TRANSLATORS: Cycle distance to place (itinerary header)
        'BICYCLE': _tr('Cycle'),
        //TRANSLATORS: Car distance to place (itinerary header)
        'CAR': _tr('Car'),
        //TRANSLATORS: Bus: (route number) Start station to end station (itinerary header)
        'BUS' : _tr('Bus'),
        'SUBWAY' : _tr('Subway'),
        'RAIL' : _tr('Train'),
        'FERRY' : _tr('Ferry'),
        'TRAM' : _tr('Light Rail'),
        'CABLE_CAR': _tr('Cable Car'),
        'FUNICULAR': _tr('Funicular'),
        'GONDOLA' : _tr('Aerial Tram'),
    },
    
    modeString : function(mode) {
        if(mode in this.modeStrings) return this.modeStrings[mode];
        return mode;
    },
    
    getLegStepText : function(step, asHtml) {
        asHtml = (typeof asHtml === "undefined") ? true : asHtml;
        var text = '';
        if(step.relativeDirection == "CIRCLE_COUNTERCLOCKWISE" || step.relativeDirection == "CIRCLE_CLOCKWISE") {
            if (step.relativeDirection == "CIRCLE_COUNTERCLOCKWISE") {
                if (asHtml) {
                    text +=  _tr('Take roundabout <b>counterclockwise</b> to <b>%s</b> exit on <b>%s</b>', otp.util.Text.ordinal(step.exit), step.streetName);
                } else {
                    text +=  _tr('Take roundabout counterclockwise to %s exit on %s', otp.util.Text.ordinal(step.exit), step.streetName);
                }
            } else {
                if (asHtml) {
                    text +=  _tr('Take roundabout <b>clockwise</b> to <b>%s</b> exit on <b>%s</b>', otp.util.Text.ordinal(step.exit), step.streetName);
                } else {
                    text +=  _tr('Take roundabout clockwise to %s exit on %s', otp.util.Text.ordinal(step.exit), step.streetName);
                }
            }

        }
        else {
            //TODO: Absolute direction translation
            //TRANSLATORS: Start on [stret name] heading [compas direction]
            if(!step.relativeDirection) text += _tr("Start on") + (asHtml ? " <b>" : " ") + step.streetName + (asHtml ? "</b>" : "") + _tr(" heading ") + step.absoluteDirection.toLowerCase();
            else {
                text += (asHtml ? "<b>" : "") + otp.util.Text.capitalizeFirstChar(this.directionString(step.relativeDirection)) +
                            (asHtml ? "</b>" : "") + ' ' +
                            //TRANSLATORS: [Relative direction (Left/Right...)] to continue
                            //on /on to [streetname]
                            (step.stayOn ? _tr("to continue on") : _tr("on to"))  + (asHtml ? " <b>" : " ") +
                            step.streetName + (asHtml ? "</b>" : "");
            }
        }
        return text;
    },
    
    getRouteDisplayString : function(routeData) {
        var str = routeData.routeShortName ? '('+routeData.routeShortName+') ' : '';
        str += routeData.routeLongName;
        return str;
    },
    
    getRouteShortReference : function(routeData) {
        return routeData.routeShortName || routeData.id.id;
    },    
}
/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.api.resource;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import org.opentripplanner.routing.bike_rental.BikeRentalStation;

@XmlRootElement(name="BikeRentalStationList1")
public class BikeRentalStationList {
    @JacksonXmlElementWrapper(localName="stations")
    @JacksonXmlProperty(localName="station")
    public List<BikeRentalStation> stations = new ArrayList<BikeRentalStation>();

    @JacksonXmlElementWrapper(localName="hubs")
    @JacksonXmlProperty(localName="hub")
    public List<BikeRentalStation> hubs = new ArrayList<BikeRentalStation>();
	
}

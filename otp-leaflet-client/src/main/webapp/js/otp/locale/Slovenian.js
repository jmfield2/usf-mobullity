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

otp.namespace("otp.locale");

/**
  * @class
  */
otp.locale.Slovenian = {

    config :
    {
        //Name of a language written in a language itself (Used in Frontend to
        //choose a language)
        name: 'Slovensko',
        //FALSE-imperial units are used
        //TRUE-Metric units are used
        metric : true, 
        //Name of localization file (*.po file) in otp-leaflet-client/src/main/webapp/i18n
        locale_short : "sl",
        //Name of datepicker localization in
        //otp-leaflet-client/src/main/webapp/js/lib/jquery-ui/i18n (usually
        //same as locale_short)
        //this is index in $.datepicker.regional array
        //If file for your language doesn't exist download it from here
        //https://github.com/jquery/jquery-ui/tree/1-9-stable/ui/i18n
        //into otp-leaflet-client/src/main/webapp/js/lib/jquery-ui/i18n
        //and add it in index.html after other localizations
        //It will be used automatically when UI is switched to this locale
        datepicker_locale_short: "sl"
    },

    /**
     * Info Widgets: a list of the non-module-specific "information widgets"
     * that can be accessed from the top bar of the client display. Expressed as
     * an array of objects, where each object has the following fields:
     * - content: <string> the HTML content of the widget
     * - [title]: <string> the title of the widget
     * - [cssClass]: <string> the name of a CSS class to apply to the widget.
     * If not specified, the default styling is used.
     */
    infoWidgets : [
            {
                title: 'O strani',
                content: '<p>Beta verzija načrtovalnika poti za Maribor.</p>' +
                '<p>Načrtovanje poti je trenutno mogoče samo v okolici Maribora (zaradi male zmogljivosti strežnika).</p>' +
                '<p>Trenutno so vključeni vozni redi Marproma in lokacije postaj za izposojo koles BCikel</p>' +
                '<p>Niso še dodane vse vožnje avtobusov:</p>' +
                '<p>Manjka vožnja 20ke preko Zrkovc. <strong>Vsi ostali postanki bi morali ustrezati dejanskemu stanju</strong></p>' +
                '<p>Temelji na <a href="http://www.opentripplanner.org/">OpenTripPlanner</a>-ju</p>',
                //cssClass: 'otp-contactWidget',
            },
            /*{
                title: 'Kontakt',
                content: '<p>Komentarji? Kontaktirate nas lahko...</p>'
            },*/
        {
            title: 'Legenda (Zapore cest)',
            content: '<p>Dodane so zapore cest Rallya in pa zaprta Tezenska ulica</p>' +
                '<p>Ko je karta oddaljena je prikazan samo znak.' +
                    ' Ko je približana pa lahko vidimo poti različnih barv:</p>' +
                    '<p>Če je cesta črtkana pomeni, da ne vemo natančno kje je zaprta. Lahko da je delno zaprta' +
                    ' Ali pa zaprt samo pločnik. Zato je prikazana črtkano oz. modro. Ni pa upoštevana v načrtovalniku.' +
                '<p><strong><span style="color:green">Zelena</span></strong> - cesta bo zaprta v prihodnosti</p>' +
                '<p><strong><span style="color:orange">Oranžna</span></strong> - cesta bo zaprta v naslednji uri</p>' +
                '<p><strong><span style="color:red">Rdeča</span></strong> - cesta je zaprta</p>' +
                '<p><strong><span style="color:blue">Modra črtkana</span></strong> - Nekaj pri cesti je zaprto</p>' +
                '<p>Načrtovalnik poti bi moral ignorirati zaprte ceste, če so zaprte v času potovanja' +
               ' (Čas potovanja se lahko spreminja v Nastavitha poti...)', 
        }
    ],


    time:
    {
        format         : "D. MM. YYYY H:mm", //momentjs
        date_format    : "DD.MM.YYYY", //momentjs
        time_format    : "H:mm", //momentjs
        time_format_picker : "HH:mm", //http://trentrichardson.com/examples/timepicker/#tp-formatting
    },


    CLASS_NAME : "otp.locale.Slovenian"
};


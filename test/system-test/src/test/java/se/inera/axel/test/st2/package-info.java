/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
/**
 * Test cases for System Test #2 includes SHS synchronous use cases, as well as RIV/SHS bridge use cases.
 *
 * <p/>
 * <h2>To be tested</h2>
 * <ol>
 * <li/>Tillse att SHS-delen i Axel är kompatibel med en godkänd SHS-server i de synkrona use-casen.
 * En delmängd av de fastlagda interoptesterna genomförs med en del variationer för att testa alternativflöden.
 * <li/>RIV/SHS-bryggan testas så att kommunikationen mellan RIV-plattformar och SHS-plattformar kan integreras enligt den överenskommelse som träffats (hänvisning?)
 * <li/>Administrationsgränssnitt testas av så att det går att administrera SHS-servern samt RIV/SHS-bryggan
 * </ol>
 *
 * <h2>Preconditions</h2>
 * Axel (System under test) byggs och installeras från given versions-tag, Referensnod (Logica SHS-test v2.1) finns installerad på en annan server.
 * Test-produkter, testdata, adresserings-uppgifter och mappningsdata förebereds enligt förljande:
 * <ul>
 <li/> Features att installera i Axel: shs-broker, shs-riv, riv-ping, shs-ping, shs-webconsole
 <li/>Axel har orgnr=000000000, Referens SHS-nod orgnr=1111111111
 <li/>SHS-Produkt 00000000-0000-0000-0000-000000000000 är en TEMPLATE-produkt och används för PING-tester i resp SHS-server. Den ska kunna ta vilken data som helst i EN datapart och svara med godtyckligt svar.
 <li/>SHS-Produkt 00000000-0000-0000-0000-000000000001 mappas i RIV/SHS bryggan till RIV-tjänsten PingForConfiguration (urn:riv:itintegration:monitoring:PingForConfigurationResponder:1) Bara Axel ska ha denna tjänst.
 <li/>SHS-Produkt 00000000-0000-0000-0000-000000000002 ska kunna "produktaddresseras" till "1111111111"
 <li/>SHS-Produkt 00000000-0000-0000-0000-000000000003 ska det finnas ett "publikt agreement" i ldap-server för "1111111111", men ingen tjänst bakom.
 <li/>SHS-Produkt 00000000-0000-0000-0000-000000000004 ska ha en address i ldap-katalogen under "1111111111" men peka på en icke-befintlig http-adress.
 <li/>SHS-Produkt 00000000-0000-0000-0000-000000000005 ska ha en mappning i riv/shs-bryggan till tjänsten MakeBooking men peka på en icke-befintlig http-adress
 <li/>Agreements finns:
 <ul>
 <li/> Mellan 000000000 och 1111111111 för 00000000-0000-0000-0000-000000000000
 <li/> Mellan 000000000 och 000000000 för 00000000-0000-0000-0000-000000000000
 <li/> Mellan 000000000 och 000000000 för 00000000-0000-0000-0000-000000000001
 <li/> Mellan 000000000 och 1111111111 för 00000000-0000-0000-0000-000000000001
 <li/> Mellan 000000000 och 1111111111 för 00000000-0000-0000-0000-000000000002
 <li/> Publikt för 1111111111 för 00000000-0000-0000-0000-000000000003 i ldapen
 <li/> Mellan 000000000 och 1111111111 för 00000000-0000-0000-0000-000000000004
 <li/> Mellan 000000000 och 000000000 för 00000000-0000-0000-0000-000000000005
 </ul>
 <li/> Produkfiler för alla produkter skapas och läggs i resp. shs-server.
 <li/> Ldap-servern konfigureras med rätt addresser för produkterna. Bara Axel ska kunna ta emot riv-shs-meddelanden från både 000000000 och 1111111111
 <li/> Axels implementation av RIV-tjänsten "PingForConfiguration" är gjord så att den returnerar SOAP-fault vid en viss indata.
 *</ul>
 *
 */
package se.inera.axel.test.st2;
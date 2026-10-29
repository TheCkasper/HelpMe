package com.helpme.app.feature.map

import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject

class LeafletMapBridge(
    private val onMarkerClick: (String) -> Unit,
    private val onMapReady: () -> Unit
) {

    @JavascriptInterface
    fun onMarkerSelected(reportId: String) {
        onMarkerClick(reportId)
    }

    @JavascriptInterface
    fun onMapLoaded() {
        onMapReady()
    }

    companion object {
        const val BRIDGE_NAME = "AndroidMapBridge"
        const val DEFAULT_COBAN_LAT = 15.4708
        const val DEFAULT_COBAN_LNG = -90.3708

        fun updateMarkersInWebView(webView: WebView, markers: List<ReportMarker>) {
            val jsonArray = JSONArray()
            for (marker in markers) {
                val obj = JSONObject().apply {
                    put("id", marker.reporteId)
                    put("lat", marker.latitud)
                    put("lng", marker.longitud)
                    put("tipo", marker.tipo)
                    put("estado", marker.estado)
                    put("descripcion", marker.descripcion)
                }
                jsonArray.put(obj)
            }
            val jsonString = jsonArray.toString()
            val jsCode = "if (typeof setMarkers === 'function') { setMarkers($jsonString); }"
            webView.post {
                webView.evaluateJavascript(jsCode, null)
            }
        }

        fun setUserLocationInWebView(webView: WebView, lat: Double, lng: Double, isUserLocation: Boolean) {
            val jsCode = "if (typeof initUserLocation === 'function') { initUserLocation($lat, $lng, $isUserLocation); }"
            webView.post {
                webView.evaluateJavascript(jsCode, null)
            }
        }

        fun getFreeMapHtml(): String {
            return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                <title>HelpMe Map</title>
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    html, body, #map {
                        height: 100%;
                        width: 100%;
                        margin: 0;
                        padding: 0;
                        background: #f8f9fa;
                    }
                    .custom-pin {
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .custom-pin svg {
                        filter: drop-shadow(0px 2px 4px rgba(0,0,0,0.3));
                    }
                    .user-location-pin {
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .user-location-pin .pulse {
                        width: 18px;
                        height: 18px;
                        background-color: #29b6f6;
                        border: 3px solid #ffffff;
                        border-radius: 50%;
                        box-shadow: 0 0 10px rgba(41, 182, 246, 0.8);
                        animation: pulse-animation 2s infinite;
                    }
                    @keyframes pulse-animation {
                        0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(41, 182, 246, 0.7); }
                        70% { transform: scale(1.15); box-shadow: 0 0 0 10px rgba(41, 182, 246, 0); }
                        100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(41, 182, 246, 0); }
                    }
                    .leaflet-popup-content-wrapper {
                        border-radius: 8px;
                        padding: 4px;
                    }
                    .popup-title {
                        font-weight: bold;
                        font-size: 14px;
                        margin-bottom: 4px;
                        color: #1a1a1a;
                    }
                    .popup-badge {
                        display: inline-block;
                        padding: 2px 8px;
                        border-radius: 12px;
                        font-size: 11px;
                        color: #ffffff;
                        font-weight: bold;
                        margin-bottom: 6px;
                    }
                    .badge-pendiente { background-color: #FFA000; }
                    .badge-proceso { background-color: #1976D2; }
                    .badge-resuelto { background-color: #388E3C; }
                    .badge-default { background-color: #757575; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var DEFAULT_LAT = 15.4708;
                    var DEFAULT_LNG = -90.3708;

                    var map = L.map('map', {
                        zoomControl: true,
                        attributionControl: true
                    }).setView([DEFAULT_LAT, DEFAULT_LNG], 13);

                    var primaryTileUrl = 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}';
                    var fallbackTileUrl = 'https://tile.openstreetmap.org/{z}/{x}/{y}.png';

                    var mainTileLayer = L.tileLayer(primaryTileUrl, {
                        attribution: 'Tiles &copy; Esri &mdash; Source: Esri, DeLorme, NAVTEQ',
                        maxZoom: 19
                    }).addTo(map);

                    mainTileLayer.on('tileerror', function(error, tile) {
                        tile.src = fallbackTileUrl
                            .replace('{z}', error.coords.z)
                            .replace('{x}', error.coords.x)
                            .replace('{y}', error.coords.y);
                    });

                    var reportMarkerGroup = L.layerGroup().addTo(map);
                    var userMarker = null;

                    function initUserLocation(lat, lng, isUserLocation) {
                        if (typeof lat === 'number' && typeof lng === 'number' && !isNaN(lat) && !isNaN(lng)) {
                            map.setView([lat, lng], isUserLocation ? 14 : 13);

                            if (isUserLocation) {
                                if (userMarker) {
                                    map.removeLayer(userMarker);
                                }
                                var userIcon = L.divIcon({
                                    className: 'user-location-pin',
                                    html: '<div class="pulse"></div>',
                                    iconSize: [24, 24],
                                    iconAnchor: [12, 12]
                                });
                                userMarker = L.marker([lat, lng], { icon: userIcon }).addTo(map);
                                userMarker.bindPopup('<div class="popup-title">Tu Ubicación Actual</div>');
                            }
                        }
                    }

                    function getPinColor(estado) {
                        if (!estado) return '#757575';
                        var e = estado.toLowerCase().trim();
                        if (e === 'pendiente') return '#FFA000';
                        if (e === 'en proceso' || e === 'proceso') return '#1976D2';
                        if (e === 'resuelto') return '#388E3C';
                        return '#757575';
                    }

                    function createCustomIcon(estado) {
                        var color = getPinColor(estado);
                        var svgHtml = '<svg width="28" height="38" viewBox="0 0 24 32" fill="none" xmlns="http://www.w3.org/2000/svg">' +
                            '<path d="M12 0C5.37 0 0 5.37 0 12C0 21 12 32 12 32C12 32 24 21 24 12C24 5.37 18.63 0 12 0Z" fill="' + color + '"/>' +
                            '<circle cx="12" cy="12" r="5" fill="white"/>' +
                            '</svg>';
                        return L.divIcon({
                            className: 'custom-pin',
                            html: svgHtml,
                            iconSize: [28, 38],
                            iconAnchor: [14, 38],
                            popupAnchor: [0, -34]
                        });
                    }

                    function escapeHtml(str) {
                        if (!str) return '';
                        return String(str)
                            .replace(/&/g, '&amp;')
                            .replace(/</g, '&lt;')
                            .replace(/>/g, '&gt;')
                            .replace(/"/g, '&quot;')
                            .replace(/'/g, '&#039;');
                    }

                    function getBadgeClass(estado) {
                        if (!estado) return 'badge-default';
                        var e = estado.toLowerCase().trim();
                        if (e === 'pendiente') return 'badge-pendiente';
                        if (e === 'en proceso' || e === 'proceso') return 'badge-proceso';
                        if (e === 'resuelto') return 'badge-resuelto';
                        return 'badge-default';
                    }

                    function setMarkers(reports) {
                        reportMarkerGroup.clearLayers();
                        if (!reports || reports.length === 0) {
                            return;
                        }

                        var bounds = [];
                        reports.forEach(function(r) {
                            if (typeof r.lat === 'number' && typeof r.lng === 'number' && !isNaN(r.lat) && !isNaN(r.lng)) {
                                var icon = createCustomIcon(r.estado);
                                var marker = L.marker([r.lat, r.lng], { icon: icon });

                                var popupHtml = '<div class="popup-title">' + escapeHtml(r.tipo) + '</div>' +
                                    '<span class="popup-badge ' + getBadgeClass(r.estado) + '">' + escapeHtml(r.estado) + '</span>' +
                                    '<div>' + escapeHtml(r.descripcion) + '</div>';

                                marker.bindPopup(popupHtml);

                                marker.on('click', function() {
                                    if (window.${BRIDGE_NAME} && typeof window.${BRIDGE_NAME}.onMarkerSelected === 'function') {
                                        window.${BRIDGE_NAME}.onMarkerSelected(r.id);
                                    }
                                });

                                reportMarkerGroup.addLayer(marker);
                                bounds.push([r.lat, r.lng]);
                            }
                        });

                        if (bounds.length > 0) {
                            map.fitBounds(bounds, { padding: [30, 30], maxZoom: 15 });
                        }
                    }

                    if (window.${BRIDGE_NAME} && typeof window.${BRIDGE_NAME}.onMapLoaded === 'function') {
                        window.${BRIDGE_NAME}.onMapLoaded();
                    }
                </script>
            </body>
            </html>
            """.trimIndent()
        }
    }
}

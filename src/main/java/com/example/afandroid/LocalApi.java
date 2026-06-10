package com.example.afandroid;

public class LocalApi {

        private String nome;
        private String tipo;
        private double latitude;
        private double longitude;
        private double distancia;

        public LocalApi() {
        }

        public LocalApi(String nome, String tipo, double latitude, double longitude, double distancia) {
            this.nome = nome;
            this.tipo = tipo;
            this.latitude = latitude;
            this.longitude = longitude;
            this.distancia = distancia;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getDistancia() {
            return distancia;
        }

        public void setDistancia(double distancia) {
            this.distancia = distancia;
        }

        @Override
        public String toString() {
            return nome +
                    "\nCategoria: " + tipo +
                    "\nDistância: " + String.format("%.0f", distancia) + " metros" +
                    "\nLat: " + latitude +
                    " | Lon: " + longitude;
        }
    }


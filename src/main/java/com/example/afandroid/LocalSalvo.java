package com.example.afandroid;

public class LocalSalvo {

    private String id;
    private String nome;
    private String categoria;
    private double latitude;
    private double longitude;
    private String observacao;
    private String finalidade;

    public LocalSalvo() {
    }

    public LocalSalvo(String id, String nome, String categoria, double latitude, double longitude, String observacao, String finalidade) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.latitude = latitude;
        this.longitude = longitude;
        this.observacao = observacao;
        this.finalidade = finalidade;
    }

    public LocalSalvo(String nome, String categoria, double latitude, double longitude, String observacao, String finalidade) {
        this.nome = nome;
        this.categoria = categoria;
        this.latitude = latitude;
        this.longitude = longitude;
        this.observacao = observacao;
        this.finalidade = finalidade;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
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

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getFinalidade() {
        return finalidade;
    }

    public void setFinalidade(String finalidade) {
        this.finalidade = finalidade;
    }

    @Override
    public String toString() {
        return nome +
                "\nCategoria: " + categoria +
                "\nFinalidade: " + finalidade +
                "\nObservação: " + observacao +
                "\nLat: " + latitude +
                " | Lon: " + longitude;
    }
}
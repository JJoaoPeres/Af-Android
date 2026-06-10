package com.example.afandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView txtLocalizacao;
    private Spinner spCategoriaBusca;
    private Spinner spFinalidade;
    private EditText edtObservacao;
    private Button btnBuscar;
    private Button btnSalvar;
    private Button btnAtualizar;
    private ListView listLugares;
    private ListView listSalvos;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    private double latitudeAtual;
    private double longitudeAtual;

    private ArrayList<LugarApi> listaLugaresApi = new ArrayList<>();
    private ArrayList<LocalSalvo> listaLocaisSalvos = new ArrayList<>();

    private ArrayAdapter<String> adapterLugaresApi;
    private ArrayAdapter<String> adapterLocaisSalvos;

    private LugarApi lugarSelecionadoApi;
    private LocalSalvo localSelecionadoSalvo;

    private String enderecoAtual = "";

    private static final int CODIGO_PERMISSAO_LOCALIZACAO = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLocalizacao = findViewById(R.id.txtLocalizacao);
        spCategoriaBusca = findViewById(R.id.spCategoriaBusca);
        spFinalidade = findViewById(R.id.spFinalidade);
        edtObservacao = findViewById(R.id.edtObservacao);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnAtualizar = findViewById(R.id.btnAtualizar);
        listLugares = findViewById(R.id.listLugares);
        listSalvos = findViewById(R.id.listSalvos);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        configurarSpinners();
        configurarListas();
        configurarBotoes();

        solicitarLocalizacao();
        carregarLocaisSalvos();
    }

    private void configurarSpinners() {
        String[] categoriasBusca = {
                "Restaurante",
                "Farmácia",
                "Hospital",
                "Escola",
                "Praça",
                "Mercado"
        };

        ArrayAdapter<String> adapterCategorias = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoriasBusca
        );
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoriaBusca.setAdapter(adapterCategorias);

        String[] finalidades = {
                "Estudo",
                "Saúde",
                "Lazer",
                "Alimentação",
                "Compras",
                "Outros"
        };

        ArrayAdapter<String> adapterFinalidades = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                finalidades
        );
        adapterFinalidades.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFinalidade.setAdapter(adapterFinalidades);
    }

    private void configurarListas() {
        adapterLugaresApi = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );

        adapterLocaisSalvos = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );

        listLugares.setAdapter(adapterLugaresApi);
        listSalvos.setAdapter(adapterLocaisSalvos);

        listLugares.setOnItemClickListener((parent, view, position, id) -> {
            lugarSelecionadoApi = listaLugaresApi.get(position);
            localSelecionadoSalvo = null;

            edtObservacao.setText("");

            Toast.makeText(
                    MainActivity.this,
                    "Local selecionado: " + lugarSelecionadoApi.nome,
                    Toast.LENGTH_SHORT
            ).show();
        });

        listSalvos.setOnItemClickListener((parent, view, position, id) -> {
            localSelecionadoSalvo = listaLocaisSalvos.get(position);
            lugarSelecionadoApi = null;

            edtObservacao.setText(localSelecionadoSalvo.observacao);

            selecionarSpinner(spFinalidade, localSelecionadoSalvo.finalidade);

            Toast.makeText(
                    MainActivity.this,
                    "Local carregado para edição",
                    Toast.LENGTH_SHORT
            ).show();
        });

        listSalvos.setOnItemLongClickListener((parent, view, position, id) -> {
            LocalSalvo local = listaLocaisSalvos.get(position);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Excluir local")
                    .setMessage("Deseja excluir o local " + local.nome + "?")
                    .setPositiveButton("Sim", (dialog, which) -> excluirLocal(local))
                    .setNegativeButton("Não", null)
                    .show();

            return true;
        });
    }

    private void configurarBotoes() {
        btnBuscar.setOnClickListener(v -> buscarLugaresProximos());

        btnSalvar.setOnClickListener(v -> salvarLocal());

        btnAtualizar.setOnClickListener(v -> atualizarLocalSalvo());
    }

    private void solicitarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PERMISSAO_LOCALIZACAO
            );

            return;
        }

        obterLocalizacaoAtual();
    }

    private void obterLocalizacaoAtual() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitudeAtual = location.getLatitude();
                        longitudeAtual = location.getLongitude();

                        txtLocalizacao.setText(
                                "Latitude: " + latitudeAtual +
                                        "\nLongitude: " + longitudeAtual
                        );
                    } else {
                        txtLocalizacao.setText("Não foi possível obter a localização.");
                    }
                });
    }

    private void buscarLugaresProximos() {
        if (latitudeAtual == 0 || longitudeAtual == 0) {
            Toast.makeText(this, "Localização ainda não capturada.", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoria = spCategoriaBusca.getSelectedItem().toString();

        new Thread(() -> {
            try {
                String consulta = montarConsultaOverpass(categoria);
                String urlString = "https://overpass-api.de/api/interpreter?data="
                        + URLEncoder.encode(consulta, "UTF-8");

                URL url = new URL(urlString);
                HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("GET");
                conexao.setRequestProperty("User-Agent", "AF-Mobile-Android");

                int responseCode = conexao.getResponseCode();

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conexao.getInputStream())
                    );

                    String linha;
                    StringBuilder resposta = new StringBuilder();

                    while ((linha = reader.readLine()) != null) {
                        resposta.append(linha);
                    }

                    reader.close();

                    Gson gson = new Gson();
                    OverpassResponse overpassResponse = gson.fromJson(
                            resposta.toString(),
                            OverpassResponse.class
                    );

                    listaLugaresApi.clear();

                    if (overpassResponse != null && overpassResponse.elements != null) {
                        for (ElementoOverpass elemento : overpassResponse.elements) {
                            if (elemento.lat != 0 && elemento.lon != 0) {
                                LugarApi lugar = new LugarApi();

                                if (elemento.tags != null && elemento.tags.name != null) {
                                    lugar.nome = elemento.tags.name;
                                } else {
                                    lugar.nome = "Local sem nome";
                                }

                                lugar.tipo = categoria;
                                lugar.latitude = elemento.lat;
                                lugar.longitude = elemento.lon;
                                lugar.distancia = calcularDistancia(
                                        latitudeAtual,
                                        longitudeAtual,
                                        elemento.lat,
                                        elemento.lon
                                );

                                listaLugaresApi.add(lugar);
                            }
                        }
                    }

                    runOnUiThread(() -> atualizarListaLugaresApi());

                } else {
                    runOnUiThread(() -> Toast.makeText(
                            MainActivity.this,
                            "Erro na API: " + responseCode,
                            Toast.LENGTH_SHORT
                    ).show());
                }

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        MainActivity.this,
                        "Erro: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
            }
        }).start();
    }

    private String montarConsultaOverpass(String categoria) {
        String chave = "amenity";
        String valor = "restaurant";

        if (categoria.equals("Restaurante")) {
            chave = "amenity";
            valor = "restaurant";
        } else if (categoria.equals("Farmácia")) {
            chave = "amenity";
            valor = "pharmacy";
        } else if (categoria.equals("Hospital")) {
            chave = "amenity";
            valor = "hospital";
        } else if (categoria.equals("Escola")) {
            chave = "amenity";
            valor = "school";
        } else if (categoria.equals("Mercado")) {
            chave = "shop";
            valor = "supermarket";
        } else if (categoria.equals("Praça")) {
            chave = "leisure";
            valor = "park";
        }

        return "[out:json];" +
                "node[\"" + chave + "\"=\"" + valor + "\"](around:1500,"
                + latitudeAtual + "," + longitudeAtual + ");" +
                "out;";
    }

    private void atualizarListaLugaresApi() {
        adapterLugaresApi.clear();

        for (LugarApi lugar : listaLugaresApi) {
            String texto = lugar.nome +
                    "\nCategoria: " + lugar.tipo +
                    "\nDistância: " + String.format("%.0f", lugar.distancia) + " metros" +
                    "\nLat: " + lugar.latitude +
                    " | Lon: " + lugar.longitude;

            adapterLugaresApi.add(texto);
        }

        adapterLugaresApi.notifyDataSetChanged();

        if (listaLugaresApi.isEmpty()) {
            Toast.makeText(this, "Nenhum local encontrado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarLocal() {
        if (lugarSelecionadoApi == null) {
            Toast.makeText(this, "Selecione um local da API primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }

        String observacao = edtObservacao.getText().toString();
        String finalidade = spFinalidade.getSelectedItem().toString();

        LocalSalvo local = new LocalSalvo();
        local.nome = lugarSelecionadoApi.nome;
        local.categoria = lugarSelecionadoApi.tipo;
        local.latitude = lugarSelecionadoApi.latitude;
        local.longitude = lugarSelecionadoApi.longitude;
        local.observacao = observacao;
        local.finalidade = finalidade;

        db.collection("locais_salvos")
                .add(local)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Local salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    edtObservacao.setText("");
                    lugarSelecionadoApi = null;
                    carregarLocaisSalvos();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao salvar: " + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show());
    }

    private void carregarLocaisSalvos() {
        db.collection("locais_salvos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaLocaisSalvos.clear();
                    adapterLocaisSalvos.clear();

                    queryDocumentSnapshots.forEach(document -> {
                        LocalSalvo local = document.toObject(LocalSalvo.class);
                        local.id = document.getId();

                        listaLocaisSalvos.add(local);

                        String texto = local.nome +
                                "\nCategoria: " + local.categoria +
                                "\nFinalidade: " + local.finalidade +
                                "\nObservação: " + local.observacao +
                                "\nLat: " + local.latitude +
                                " | Lon: " + local.longitude;

                        adapterLocaisSalvos.add(texto);
                    });

                    adapterLocaisSalvos.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao carregar locais salvos.",
                        Toast.LENGTH_SHORT
                ).show());
    }

    private void atualizarLocalSalvo() {
        if (localSelecionadoSalvo == null) {
            Toast.makeText(this, "Selecione um local salvo para editar.", Toast.LENGTH_SHORT).show();
            return;
        }

        String novaObservacao = edtObservacao.getText().toString();
        String novaFinalidade = spFinalidade.getSelectedItem().toString();

        db.collection("locais_salvos")
                .document(localSelecionadoSalvo.id)
                .update(
                        "observacao", novaObservacao,
                        "finalidade", novaFinalidade
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Local atualizado!", Toast.LENGTH_SHORT).show();
                    edtObservacao.setText("");
                    localSelecionadoSalvo = null;
                    carregarLocaisSalvos();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao atualizar: " + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show());
    }

    private void excluirLocal(LocalSalvo local) {
        db.collection("locais_salvos")
                .document(local.id)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Local excluído!", Toast.LENGTH_SHORT).show();
                    carregarLocaisSalvos();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao excluir: " + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show());
    }

    private void selecionarSpinner(Spinner spinner, String valor) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();

        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(valor)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private double calcularDistancia(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {
        float[] resultado = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, resultado);
        return resultado[0];
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODIGO_PERMISSAO_LOCALIZACAO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obterLocalizacaoAtual();
            } else {
                txtLocalizacao.setText("Permissão de localização negada.");
                Toast.makeText(
                        this,
                        "Não é possível buscar locais sem permissão de localização.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    public static class LugarApi {
        public String nome;
        public String tipo;
        public double latitude;
        public double longitude;
        public double distancia;

        public LugarApi() {
        }
    }

    public static class LocalSalvo {
        public String id;
        public String nome;
        public String categoria;
        public double latitude;
        public double longitude;
        public String observacao;
        public String finalidade;

        public LocalSalvo() {
        }
    }

    public static class OverpassResponse {
        public List<ElementoOverpass> elements;
    }

    public static class ElementoOverpass {
        public long id;
        public double lat;
        public double lon;
        public Tags tags;
    }

    public static class Tags {
        public String name;
        public String amenity;
        public String shop;
        public String leisure;
    }
}
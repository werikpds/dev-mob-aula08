package aula8.com.br.chat_firebase;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class ChatActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitudeAtual;
    private double longitudeAtual;
    private String coordenada;

    private static final int REQUEST_CODE_GPS = 1001;
    private RecyclerView mensagensRecyclerView;
    private ChatAdapter adapter;
    private List <Mensagem> mensagens;
    private EditText mensagemEditText;
    private TextView coordenadaTextView;

    private CollectionReference mMsgsReference;
    private FirebaseUser fireUser;

    private void setupFirebase(){
        System.out.println("\nEntrou no setupFirebase\n");
        mMsgsReference =
                FirebaseFirestore.getInstance().collection(
                        "mensagens"
                );
        fireUser = FirebaseAuth.getInstance().getCurrentUser();
        mMsgsReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                mensagens.clear();
                for (DocumentSnapshot document :
                        queryDocumentSnapshots.getDocuments()){
                    Mensagem m = document.toObject(Mensagem.class);
                    mensagens.add(m);
                }
                Collections.sort(mensagens);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setupRecyclerView (){
        System.out.println("\nEntrou no setupRecyvlerView\n");
        mensagensRecyclerView = findViewById(R.id.mensagensRecyclerView);
        mensagens = new ArrayList<>();
        adapter = new ChatAdapter (this, mensagens);
        mensagensRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );
        mensagensRecyclerView.setAdapter(adapter);
    }

    private void setupViews (){
        System.out.println("\nEntrou no setupViews\n");
        mensagemEditText = findViewById(R.id.mensagemEditText);
        //coordenadaTextView = findViewById(R.id.coordenadaTextView);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setupRecyclerView();
        setupViews();
        setupFirebase();
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                System.out.println("\nEntrou no onLocationChanged\n");
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                latitudeAtual = lat;
                longitudeAtual = lon;
                System.out.println("\nLat: " + lat + latitudeAtual + "Lon: " + lon + longitudeAtual + "\n");
                coordenada = String.format("Lat: %f,\nLong: %f", lat, lon);
                System.out.println("\nCoordenada1: " + coordenada + "\n");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    public void enviarMensagem (View v){
        String texto = mensagemEditText.getText().toString();
        Mensagem m = new Mensagem (fireUser.getEmail(), new Date(), texto);
        m.setType("texto");

        esconderTeclado(v);
        mMsgsReference.add(m);
        mensagemEditText.setText("");
        Toast.makeText(
                this,
                getString(R.string.msg_enviada),
                Toast.LENGTH_SHORT
        ).show();
    }

    public void mostraLocal(View v) {
        System.out.println("\n Coordenada2: " + coordenada + "\n");
        String c = coordenada;
        Mensagem m = new Mensagem (fireUser.getEmail(), new Date(), c);
        m.setType("local");
        System.out.println("Antes de salvar");
        mMsgsReference.add(m);
        System.out.println("Depois de salvar");
        Toast.makeText(
                this,
                getString(R.string.msg_enviada),
                Toast.LENGTH_SHORT
        ).show();
    }

    public void mostraMapa(View v) {
        System.out.println("\n Coordenada3: " + coordenada + "\n");
        System.out.println("\n Lat: " + latitudeAtual + "Lon: " + longitudeAtual + "\n");
//        String lat = coordenada.split(",")[0].split(":")[1];
//        String lon = coordenada.split(",")[1].split(":")[1];
        Uri gmmIntentUri = Uri.parse(String.format("geo:%s,%s", latitudeAtual, longitudeAtual));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void esconderTeclado (View v) {
        InputMethodManager ims = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        ims.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //a permissão já foi dada?
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
        //somente ativa
        // a localização é obtida via hardware, intervalo de 0 segundos e 0 metros entre atualizações
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener);
        }
        else{
        //permissão ainda não foi nada, solicita ao usuário
        //quando o usuário responder, o método onRequestPermissionsResult vai ser chamado
            ActivityCompat.requestPermissions(this,
                    new String []{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_GPS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_GPS){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //permissão concedida, ativamos o GPS
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            0, 0, locationListener);
                }
            }
            else{
                //usuário negou, não ativamos
                Toast.makeText(this,
                        getString(R.string.no_gps_no_app), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

}


class ChatAdapter extends RecyclerView.Adapter <ChatViewHolder>{
    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;

    private Context context;
    private List<Mensagem> mensagens;

    public ChatAdapter(Context context, List<Mensagem> mensagens) {
        System.out.println("\nEntrou no ChatAdapter\n");
        this.context = context;
        this.mensagens = mensagens;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        System.out.println("\nEntrou no onCreateViewHolder\n");
        System.out.println("\n" + viewType + "\n");
        if (!(viewType == LEFT_MSG)) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View raiz = inflater.inflate(
                    R.layout.list_item,
                    parent,
                    false
            );
            return new ChatViewHolder(raiz);
        } else {
            LayoutInflater inflater = LayoutInflater.from(context);
            View raiz = inflater.inflate(
                    R.layout.list_place,
                    parent,
                    false
            );
            return new ChatViewHolder(raiz);
        }
    }

    @Override
    public int getItemViewType(int position) {
        System.out.println("\nEntrou no getItemViewType\n");
        Mensagem m = mensagens.get(position);
        System.out.println("\n" + m.getType() + "\n");
        if (m.getType() != null){
            if (m.getType().equals("texto")){
                return RIGHT_MSG;
            }else{
                return LEFT_MSG;
            }
        }else{
            return RIGHT_MSG;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        System.out.println("\nEntrou no onBindViewHolder\n");
        Mensagem m = mensagens.get(position);
        System.out.println("\n" + m.getType() + "\n");
        if (m.getType() == null) {
            System.out.println("\nEntrou no onBindViewHolder, type = nulo\n");
            holder.mensagemTextView.setText(m.getTexto());
            holder.dataNomeTextView.setText(
                    context.getString(
                            R.string.data_nome,
                            DateHelper.format(m.getData()),
                            m.getUsuario()
                    )
            );
        }
        else {
            if (m.getType().equals("texto")) {
                System.out.println("\nEntrou no onBindViewHolder, type = texto " + m.getType() + "\n");
//              holder.mensagemTextView.setVisibility(View.VISIBLE);
                holder.mensagemTextView.setText(m.getTexto());
                holder.dataNomeTextView.setText(
                      context.getString(
                              R.string.data_nome,
                              DateHelper.format(m.getData()),
                              m.getUsuario()
                      )
                );
//               holder.verMapaButton.setVisibility(View.GONE);
//               holder.coordenadaTextView.setVisibility(View.GONE);
            } else {
                System.out.println("\nEntrou no onBindViewHolder, type = local " + m.getType() + "\n");
//              holder.coordenadaTextView.setVisibility(View.VISIBLE);
                holder.coordenadaTextView.setText(m.getTexto());
                holder.dataNomeTextView.setText(
                      context.getString(
                              R.string.data_nome,
                              DateHelper.format(m.getData()),
                              m.getUsuario()
                      )
                );
                holder.verMapaButton.setVisibility(View.VISIBLE);
//              holder.mensagemTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }
}

class ChatViewHolder extends RecyclerView.ViewHolder {

    TextView dataNomeTextView;
    TextView mensagemTextView;
    TextView coordenadaTextView;
    Button verMapaButton;

    public ChatViewHolder (View raiz){
        super (raiz);
        System.out.println("\nEntrou no ChatViewHolder\n");
        dataNomeTextView = raiz.findViewById(R.id.dataNomeTextView);
        mensagemTextView = raiz.findViewById(R.id.mensagemTextView);
        coordenadaTextView = raiz.findViewById(R.id.coordenadaTextView);
        verMapaButton = raiz.findViewById(R.id.verMapaButton);
    }
}
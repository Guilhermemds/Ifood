package com.example.ifood.ifood.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.example.ifood.ifood.R;
import com.example.ifood.ifood.helper.ConfiguracaoFirebase;
import com.example.ifood.ifood.helper.UsuarioFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class AutenticacaoActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private Switch tipoAcesso, tipoUsuario;
    private FirebaseAuth autenticacao;
    private LinearLayout linearTipoUsuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autenticacao);



        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signOut();

        //verificaUsuarioLogdao
        verificarUsuarioLogado();

        tipoAcesso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    linearTipoUsuario.setVisibility(View.VISIBLE);
                }else{
                    linearTipoUsuario.setVisibility(View.GONE);
                }
            }
        });
        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                if(!email.isEmpty()){
                    if( !senha.isEmpty()){
                        if(tipoAcesso.isChecked()){
                            autenticacao.createUserWithEmailAndPassword(email, senha)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                          Toast.makeText(AutenticacaoActivity.this, "Cadastro realizado com sucesso !", Toast.LENGTH_SHORT).show();
                                          String tipoUsuario = getTipoUsuario();
                                          UsuarioFirebase.atualizarTipoUsuario(tipoUsuario);

                                          abrirTelaPrincipal(tipoUsuario);
                                        }else{
                                            String erroExecao = "";
                                            try{
                                                throw task.getException();
                                            }catch (FirebaseAuthWeakPasswordException e) {
                                                erroExecao = "Digite uma senha mais forte!";
                                            }catch (FirebaseAuthInvalidCredentialsException e) {
                                                erroExecao = "Por favor, digite um e-mail válido";
                                            }catch (FirebaseAuthUserCollisionException e){
                                                erroExecao = "Essa conta já foi cadastrada";
                                            }catch(Exception e) {
                                                erroExecao = "ao cadastrar usuário: " +e.getMessage();
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                        }else{//login
                            autenticacao.signInWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                       Toast.makeText(AutenticacaoActivity.this, "Logado com sucesso", Toast.LENGTH_SHORT).show();
                                       String tipoUsuario = task.getResult().getUser().getDisplayName();
                                       abrirTelaPrincipal(tipoUsuario);
                                    }else{
                                        Toast.makeText(AutenticacaoActivity.this, "Erro ao fazer login", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    }else{
                         Toast.makeText(AutenticacaoActivity.this, "Preencha a senha", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(AutenticacaoActivity.this, "Preencha o E-mail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getTipoUsuario(){
        return tipoUsuario.isChecked() ? "E" : "U";
    }

    private void verificarUsuarioLogado(){
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if(usuarioAtual != null){
            String tipoUsuario = usuarioAtual.getDisplayName();
            abrirTelaPrincipal(tipoUsuario);
        }
    }

    private void abrirTelaPrincipal(String tipoUsuario){
        if(tipoUsuario.equals("E")){//empresa
            startActivity(new Intent(getApplicationContext(), EmpresaActivity.class));
        }else{//Usuario
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }
    }

    private void inicializarComponentes(){
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        tipoAcesso = findViewById(R.id.switchAcesso);
        botaoAcessar = findViewById(R.id.buttonAcesso);
        tipoUsuario = findViewById(R.id.switchTipoUsuario);
        linearTipoUsuario = findViewById(R.id.linearTipoUsuario);
    }
}

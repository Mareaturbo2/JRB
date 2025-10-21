export default function App() {
  return (
    <div className="container">
      <h1 className="title">JRB Bank</h1>
      <p className="subtitle">Bem-vindo! Acesse ou crie sua conta.</p>
      <div className="buttons">
        <a className="btn login" href="/login">Login</a>
        <a className="btn cadastro" href="/cadastro">Cadastro</a>
      </div>
    </div>
  );
}

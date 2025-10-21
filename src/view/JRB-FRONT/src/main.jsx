import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import App from "./App.jsx";
import Login from "./pages/Login.jsx";
import Cadastro from "./pages/Cadastro.jsx";
import Menu from "./pages/Menu.jsx";
import Saldo from "./pages/Saldo.jsx";
import Deposito from "./pages/Deposito.jsx";
import Saque from "./pages/Saque.jsx";
import Extrato from "./pages/Extrato.jsx";
import Pagamento from "./pages/Pagamento.jsx";
import Transferencia from "./pages/Transferencia.jsx";
import Cartoes from "./pages/Cartoes.jsx";
import Encerrar from "./pages/Encerrar.jsx";
import PrivateRoute from "./components/PrivateRoute.jsx";
import InvestirPoupanca from "./pages/InvestirPoupanca";
import ResgatarPoupanca from "./pages/ResgatarPoupanca";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        {/* Rota pública */}
        <Route path="/" element={<App />} />
        <Route path="/login" element={<Login />} />
        <Route path="/cadastro" element={<Cadastro />} />

        {/* 🔒 Rotas protegidas */}
        <Route
          path="/menu"
          element={
            <PrivateRoute>
              <Menu />
            </PrivateRoute>
          }
        />
        <Route
          path="/saldo"
          element={
            <PrivateRoute>
              <Saldo />
            </PrivateRoute>
          }
        />
        <Route
          path="/deposito"
          element={
            <PrivateRoute>
              <Deposito />
            </PrivateRoute>
          }
        />
        <Route
          path="/saque"
          element={
            <PrivateRoute>
              <Saque />
            </PrivateRoute>
          }
        />
        <Route
          path="/extrato"
          element={
            <PrivateRoute>
              <Extrato />
            </PrivateRoute>
          }
        />
        <Route
          path="/pagamento"
          element={
            <PrivateRoute>
              <Pagamento />
            </PrivateRoute>
          }
        />
        <Route
          path="/transferencia"
          element={
            <PrivateRoute>
              <Transferencia />
            </PrivateRoute>
          }
        />
        <Route
          path="/cartoes"
          element={
            <PrivateRoute>
              <Cartoes />
            </PrivateRoute>
          }
        />
        <Route
          path="/encerrar"
          element={
            <PrivateRoute>
              <Encerrar />
            </PrivateRoute>
          }
          
        />
        <Route
  path="/investir-poupanca"
  element={
    <PrivateRoute>
      <InvestirPoupanca />
    </PrivateRoute>
  }
/>

<Route
  path="/resgatar-poupanca"
  element={
    <PrivateRoute>
      <ResgatarPoupanca />
    </PrivateRoute>
  }
/>

      </Routes>
    </BrowserRouter>
  </React.StrictMode>
);

import { Navigate } from "react-router-dom";

export default function PrivateRoute({ children }) {
  const usuario = localStorage.getItem("usuario");
  return usuario ? children : <Navigate to="/login" replace />;
}

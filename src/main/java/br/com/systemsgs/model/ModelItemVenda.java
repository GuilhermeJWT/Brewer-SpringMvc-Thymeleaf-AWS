package br.com.systemsgs.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class ModelItemVenda implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Long codigo;
	private Integer quantidade;
	private BigDecimal valorUnitario;
	private ModelCerveja cerveja;
	
	public Long getCodigo() {
		return codigo;
	}
	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}
	public Integer getQuantidade() {
		return quantidade;
	}
	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}
	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}
	public void setValorUnitario(BigDecimal valorUnitario) {
		this.valorUnitario = valorUnitario;
	}
	public ModelCerveja getCerveja() {
		return cerveja;
	}
	public void setCerveja(ModelCerveja cerveja) {
		this.cerveja = cerveja;
	}
	
	public BigDecimal getValorTotal() {
		return valorUnitario.multiply(new BigDecimal(quantidade));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo == null) ? 0 : codigo.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelItemVenda other = (ModelItemVenda) obj;
		if (codigo == null) {
			if (other.codigo != null)
				return false;
		} else if (!codigo.equals(other.codigo))
			return false;
		return true;
	}
	
}

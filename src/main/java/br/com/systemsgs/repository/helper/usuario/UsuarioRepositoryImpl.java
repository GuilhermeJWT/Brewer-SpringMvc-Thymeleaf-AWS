package br.com.systemsgs.repository.helper.usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.com.systemgs.util.UsuarioFilter;
import br.com.systemsgs.model.ModelGrupo;
import br.com.systemsgs.model.ModelUsuario;
import br.com.systemsgs.model.ModelUsuarioGrupo;
import br.com.systemsgs.repository.PaginacaoUtil;

public class UsuarioRepositoryImpl implements UsuariosQueries{
	
	@PersistenceContext
	private EntityManager manager;
	
	@Autowired
	private PaginacaoUtil paginacaoUtil;

	@Override
	public Optional<ModelUsuario> porEmailEAtivo(String email) {
		return manager.
				createQuery("from ModelUsuario where lower(email) = lower(:email) and ativo = true", ModelUsuario.class)
				.setParameter("email", email).getResultList().stream().findFirst();
	}

	@Override
	public List<String> permissoes(ModelUsuario modelUsuario) {
		return manager.createQuery("select distinct p.nome from ModelUsuario u inner join u.grupos g inner join g.permissoes p where u =:modelUsuario",  String.class)
				.setParameter("modelUsuario", modelUsuario)
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public Page<ModelUsuario> filtrar(UsuarioFilter filtro, Pageable pageable) {
		Criteria criteria = manager.unwrap(Session.class).createCriteria(ModelUsuario.class);
		
		paginacaoUtil.preparar(criteria, pageable);
		adicionarFiltro(filtro, criteria);
		
		List<ModelUsuario> filtrados = criteria.list();
		filtrados.forEach(u -> Hibernate.initialize(u.getGrupos()));
		return new PageImpl<>(filtrados, pageable, total(filtro));
	}
	
	private Long total(UsuarioFilter filtro) {
		Criteria criteria = manager.unwrap(Session.class).createCriteria(ModelUsuario.class);
		adicionarFiltro(filtro, criteria);
		criteria.setProjection(Projections.rowCount());
		return (Long) criteria.uniqueResult();
	}

	private void adicionarFiltro(UsuarioFilter filtro, Criteria criteria) {
		if (filtro != null) {
			if (!StringUtils.isEmpty(filtro.getNome())) {
				criteria.add(Restrictions.ilike("nome", filtro.getNome(), MatchMode.ANYWHERE));
			}
			
			if (!StringUtils.isEmpty(filtro.getEmail())) {
				criteria.add(Restrictions.ilike("email", filtro.getEmail(), MatchMode.START));
			}
			
			if (filtro.getGrupos() != null && !filtro.getGrupos().isEmpty()) {
				List<Criterion> subqueries = new ArrayList<>();
				for (Long codigoGrupo : filtro.getGrupos().stream().mapToLong(ModelGrupo::getCodigo).toArray()) {
					DetachedCriteria dc = DetachedCriteria.forClass(ModelUsuarioGrupo.class);
					dc.add(Restrictions.eq("id.grupo.codigo", codigoGrupo));
					dc.setProjection(Projections.property("id.usuario"));
					
					subqueries.add(Subqueries.propertyIn("codigo", dc));
				}
				
				Criterion[] criterions = new Criterion[subqueries.size()];
				criteria.add(Restrictions.and(subqueries.toArray(criterions)));
			}
		}
	}

}

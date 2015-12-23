package org.sigmah.client.ui.presenter.project;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;

import org.sigmah.client.dispatch.CommandResultHandler;
import org.sigmah.client.dispatch.monitor.LoadingMask;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.inject.Injector;
import org.sigmah.client.page.Page;
import org.sigmah.client.page.PageRequest;
import org.sigmah.client.ui.notif.N10N;
import org.sigmah.client.ui.view.project.ProjectTeamMembersView;
import org.sigmah.client.ui.widget.button.Button;
import org.sigmah.shared.command.GetProjectTeamMembers;
import org.sigmah.shared.command.UpdateProjectTeamMembers;
import org.sigmah.shared.command.result.TeamMembersResult;
import org.sigmah.shared.dto.TeamMemberDTO;
import org.sigmah.shared.dto.UserDTO;

/**
 * Project's details presenter which manages the {@link ProjectTeamMembersView}.
 *
 * @author Aurélien PONÇON (aurelien.poncon@gmail.com)
 */
@Singleton
public class ProjectTeamMembersPresenter extends AbstractProjectPresenter<ProjectTeamMembersPresenter.View> {
	/**
	 * Description of the view managed by this presenter.
	 */
	@ImplementedBy(ProjectTeamMembersView.class)
	public interface View extends AbstractProjectPresenter.View {
		LayoutContainer getMainPanel();

		Button getSaveButton();

		ListStore<ModelData> getTeamMembersStore();

		void setRemoveTeamMemberButtonCreationHandler(RemoveTeamMemberButtonCreationHandler removeTeamMemberButtonCreationHandler);
	}

	public interface RemoveTeamMemberButtonCreationHandler {
		void onCreateRemoveUserButton(Button button, UserDTO userDTO);
	}

	private boolean modified;

	/**
	 * Presenters's initialization.
	 *
	 * @param view
	 *          Presenter's view interface.
	 * @param injector
	 *          Injected client injector.
	 */
	@Inject
	public ProjectTeamMembersPresenter(final View view, final Injector injector) {
		super(view, injector);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Page getPage() {
		return Page.PROJECT_TEAM_MEMBERS;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPageRequest(final PageRequest request) {

		load();

		modified = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasValueChanged() {
		return modified;
	}

	// ---------------------------------------------------------------------------------------------------------------
	//
	// UTILITY METHODS.
	//
	// ---------------------------------------------------------------------------------------------------------------

	/**
	 * Loads the presenter with the given project {@code team members}.
	 */
	private void load() {
		view.getTeamMembersStore().removeAll();

		dispatch.execute(new GetProjectTeamMembers(getProject().getId()), new CommandResultHandler<TeamMembersResult>() {
			@Override
			protected void onCommandSuccess(TeamMembersResult result) {
				fillTeamMembersStore(result);
			}
		}, null, new LoadingMask(view.getMainPanel()));
	}

	@Override
	public void onBind() {
		super.onBind();

		view.setRemoveTeamMemberButtonCreationHandler(new RemoveTeamMemberButtonCreationHandler() {
			@Override
			public void onCreateRemoveUserButton(Button button, final UserDTO userDTO) {
				// TODO: Verify if the user is allowed to update team members
				button.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent event) {
						view.getTeamMembersStore().remove(userDTO);
						modified = true;
						view.getSaveButton().setEnabled(true);
					}
				});
			}
		});

		view.getSaveButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			@SuppressWarnings("unchecked")
			public void componentSelected(ButtonEvent event) {
				List<UserDTO> teamMembers = (List<UserDTO>) (List) view.getTeamMembersStore().findModels(TeamMemberDTO.TYPE,
						TeamMemberDTO.TeamMemberType.TEAM_MEMBER);
				dispatch.execute(
						new UpdateProjectTeamMembers(getProject().getId(), teamMembers),
						new CommandResultHandler<TeamMembersResult>() {

							@Override
							public void onCommandFailure(final Throwable caught) {
								N10N.error(I18N.CONSTANTS.save(), I18N.CONSTANTS.saveError());
							}

							@Override
							protected void onCommandSuccess(TeamMembersResult result) {
								N10N.infoNotif(I18N.CONSTANTS.infoConfirmation(), I18N.CONSTANTS.saveConfirm());

								fillTeamMembersStore(result);
							}
						},
						view.getSaveButton(), new LoadingMask(view.getMainPanel())
				);
				modified = false;
				view.getSaveButton().setEnabled(false);
			}
		});
	}

	private void fillTeamMembersStore(TeamMembersResult result) {
		UserDTO projectManager = result.getProjectManager();
		// As the ListStore doesn't support duplicated IDs, let's modify the ID of the manager
		projectManager.set(TeamMemberDTO.ID, Integer.MAX_VALUE);
		projectManager.set(TeamMemberDTO.TYPE, TeamMemberDTO.TeamMemberType.MANAGER);
		projectManager.set(TeamMemberDTO.ORDER, 1);

		List<UserDTO> teamMembers = result.getTeamMembers();
		for (UserDTO userDTO : teamMembers) {
			userDTO.set(TeamMemberDTO.TYPE, TeamMemberDTO.TeamMemberType.TEAM_MEMBER);
			userDTO.set(TeamMemberDTO.ORDER, 3);
		}

		view.getTeamMembersStore().removeAll();
		view.getTeamMembersStore().add(projectManager);
		view.getTeamMembersStore().add(teamMembers);
	}
}

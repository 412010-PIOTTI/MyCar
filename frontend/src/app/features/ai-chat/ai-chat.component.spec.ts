import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AiChatComponent } from './ai-chat.component';
import { VehicleService } from '../../core/services/vehicle.service';
import { ChatService } from '../../core/services/chat.service';
import { Vehicle } from '../../core/models/vehicle.model';
import { ChatResponse } from '../../core/models/chat.model';

describe('AiChatComponent', () => {
  const corolla: Vehicle = {
    id: 1, plate: 'AB123CD', brand: 'Toyota', model: 'Corolla', year: 2020,
    color: 'Blanco', currentKm: 35000, createdAt: '2025-01-01T00:00:00',
  };
  const focus: Vehicle = {
    id: 2, plate: 'XY999ZZ', brand: 'Ford', model: 'Focus', year: 2018,
    color: 'Negro', currentKm: 50000, createdAt: '2025-02-01T00:00:00',
  };

  let vehicleServiceSpy: jasmine.SpyObj<VehicleService>;
  let chatServiceSpy: jasmine.SpyObj<ChatService>;

  async function setup(vehicles: Vehicle[]) {
    vehicleServiceSpy = jasmine.createSpyObj('VehicleService', ['getVehicles']);
    chatServiceSpy = jasmine.createSpyObj('ChatService', ['sendMessage']);
    vehicleServiceSpy.getVehicles.and.returnValue(of(vehicles));

    await TestBed.configureTestingModule({
      imports: [AiChatComponent],
      providers: [
        provideRouter([]),
        { provide: VehicleService, useValue: vehicleServiceSpy },
        { provide: ChatService, useValue: chatServiceSpy },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(AiChatComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', async () => {
    const fixture = await setup([corolla]);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('selects the first vehicle and shows a welcome message once vehicles load', async () => {
    const fixture = await setup([corolla, focus]);
    const component = fixture.componentInstance;

    expect(component.selectedVehicle?.id).toBe(1);
    expect(component.messages.length).toBe(1);
    expect(component.messages[0].role).toBe('assistant');
    expect(component.messages[0].text).toContain('Toyota Corolla');
  });

  it('shows no vehicles selected when the user has none, without erroring', async () => {
    const fixture = await setup([]);
    const component = fixture.componentInstance;

    expect(component.selectedVehicle).toBeNull();
    expect(component.vehicles.length).toBe(0);
  });

  it('switching vehicles keeps a separate history per vehicle', async () => {
    const fixture = await setup([corolla, focus]);
    const component = fixture.componentInstance;

    component.selectVehicle(focus);
    expect(component.selectedVehicle?.id).toBe(2);
    expect(component.messages[0].text).toContain('Ford Focus');

    component.selectVehicle(corolla);
    expect(component.messages[0].text).toContain('Toyota Corolla');
  });

  it('sending a message calls ChatService with the selected vehicle and appends the reply', async () => {
    const fixture = await setup([corolla]);
    const component = fixture.componentInstance;
    const reply: ChatResponse = { reply: 'Cada 10.000 km.', manualGrounded: true, vehicleId: 1 };
    chatServiceSpy.sendMessage.and.returnValue(of(reply));

    component.draft = '¿Cuándo toca el cambio de aceite?';
    component.send();

    expect(chatServiceSpy.sendMessage).toHaveBeenCalledWith({
      vehicleId: 1,
      message: '¿Cuándo toca el cambio de aceite?',
    });
    expect(component.messages.length).toBe(3); // welcome + user + assistant
    expect(component.messages[1].role).toBe('user');
    expect(component.messages[2].role).toBe('assistant');
    expect(component.messages[2].text).toBe('Cada 10.000 km.');
    expect(component.messages[2].manualGrounded).toBeTrue();
    expect(component.draft).toBe('');
  });

  it('does not send a blank message', async () => {
    const fixture = await setup([corolla]);
    const component = fixture.componentInstance;

    component.draft = '   ';
    component.send();

    expect(chatServiceSpy.sendMessage).not.toHaveBeenCalled();
  });

  it('surfaces an error state when the backend call fails', async () => {
    const fixture = await setup([corolla]);
    const component = fixture.componentInstance;
    chatServiceSpy.sendMessage.and.returnValue(throwError(() => new Error('network error')));

    component.draft = 'hola';
    component.send();

    expect(component.sendError).toBeTrue();
    expect(component.sending).toBeFalse();
  });

  it('a suggestion click sends its title as the message', async () => {
    const fixture = await setup([corolla]);
    const component = fixture.componentInstance;
    chatServiceSpy.sendMessage.and.returnValue(
      of<ChatResponse>({ reply: 'ok', manualGrounded: true, vehicleId: 1 }),
    );

    component.sendSuggestion('Presión de neumáticos');

    expect(chatServiceSpy.sendMessage).toHaveBeenCalledWith({
      vehicleId: 1,
      message: 'Presión de neumáticos',
    });
  });
});
